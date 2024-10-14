package com.sd.lib.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

interface FSyncable<T> {
   /** 同步并等待结果 */
   suspend fun syncWithResult(): Result<T>
}

/**
 * 当外部调用[FSyncable]的同步方法时，如果[FSyncable]处于空闲状态，则当前协程会切换到主线程执行[onSync]，
 * 如果执行未完成时又有新协程调用同步方法，则新协程会挂起等待结果。
 */
fun <T> FSyncable(
   onSync: suspend () -> T,
): FSyncable<T> = SyncableImpl(onSync = onSync)

private class SyncableImpl<T>(
   private val onSync: suspend () -> T,
) : FSyncable<T> {
   private var _isSync = false
   private val _dispatcher = Dispatchers.fMain
   private val _continuations = FContinuations<Result<T>>()

   override suspend fun syncWithResult(): Result<T> {
      if (currentCoroutineContext()[SyncElement] != null) {
         throw ReSyncException("Can not call sync() in the onSync block.")
      }
      return withContext(_dispatcher) {
         if (_isSync) {
            _continuations.await()
         } else {
            startSync().also { check(!_isSync) }
               .onSuccess {
                  _continuations.resumeAll(Result.success(it))
               }
               .onFailure {
                  if (it is ReSyncException) throw it
                  _continuations.resumeAll(Result.failure(it))
                  if (it is CancellationException) throw it
               }
         }
      }
   }

   private suspend fun startSync(): Result<T> {
      check(!_isSync)
      _isSync = true
      return runCatching {
         withContext(SyncElement()) {
            onSync()
         }.also {
            currentCoroutineContext().ensureActive()
         }
      }.also {
         check(_isSync)
         _isSync = false
      }
   }
}

private class SyncElement : AbstractCoroutineContextElement(SyncElement) {
   companion object Key : CoroutineContext.Key<SyncElement>
}

private class ReSyncException(message: String) : RuntimeException(message)