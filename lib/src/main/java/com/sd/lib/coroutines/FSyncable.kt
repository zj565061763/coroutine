package com.sd.lib.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

interface FSyncable<T> {
   /**
    * 开始同步并等待结果
    */
   suspend fun sync(): Result<T>
}

/**
 * 如果调用[FSyncable.sync]时，[FSyncable]处于空闲状态，则当前协程会切换到主线程执行[onSync]，
 * 如果执行未完成时又有新协程调用[FSyncable.sync]，则新协程会挂起等待结果，
 * 如果执行发生异常(包括取消异常)，则新协程收到的[Result]包含该异常。
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

   override suspend fun sync(): Result<T> {
      return withContext(_dispatcher) {
         if (_isSync) {
            _continuations.await()
         } else {
            startSync()
               .onSuccess {
                  _continuations.resumeAll(Result.success(it))
               }
               .onFailure {
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
         onSync().also {
            currentCoroutineContext().ensureActive()
         }
      }.also {
         check(_isSync)
         _isSync = false
      }
   }
}