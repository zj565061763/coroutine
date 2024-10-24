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
   suspend fun syncOrThrow(): T

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
   private val _continuations = FContinuations<Result<T>>()

   override suspend fun syncOrThrow(): T {
      return syncWithResult().getOrThrow()
   }

   override suspend fun syncWithResult(): Result<T> {
      if (currentCoroutineContext()[SyncElement]?.syncable == this@SyncableImpl) {
         throw ReSyncException("Can not call sync() in the onSync block.")
      }
      return withContext(Dispatchers.fMain) {
         if (_isSync) {
            _continuations.await()
         } else {
            runCatching { startSync() }.also { check(!_isSync) }
               .onSuccess { data ->
                  _continuations.resumeAll(Result.success(data))
               }
               .onFailure { error ->
                  when (error) {
                     is ReSyncException,
                     is CancellationException,
                        -> {
                        _continuations.cancelAll()
                        throw error
                     }
                     else -> {
                        _continuations.resumeAll(Result.failure(error))
                     }
                  }
               }
         }
      }
   }

   private suspend fun startSync(): T {
      check(!_isSync)
      return try {
         _isSync = true
         withContext(SyncElement(this@SyncableImpl)) {
            onSync()
         }.also {
            currentCoroutineContext().ensureActive()
         }
      } finally {
         check(_isSync)
         _isSync = false
      }
   }
}

private class SyncElement(
   val syncable: FSyncable<*>,
) : AbstractCoroutineContextElement(SyncElement) {
   companion object Key : CoroutineContext.Key<SyncElement>
}

private class ReSyncException(message: String) : RuntimeException(message)