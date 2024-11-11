package com.sd.lib.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

interface FSyncable<T> {
   /** 是否正在同步中 */
   val isSyncing: Boolean

   /** 是否正在同步中状态流 */
   val syncingFlow: Flow<Boolean>

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

/**
 * 如果当前[FSyncable]处于同步中，则会挂起直到同步结束
 */
suspend fun FSyncable<*>.awaitIdle() {
   syncingFlow.first { !it }
}

private class SyncableImpl<T>(
   private val onSync: suspend () -> T,
) : FSyncable<T> {
   private val _continuations = FContinuations<Result<T>>()
   private val _syncingFlow = MutableStateFlow(false)

   private var _syncing: Boolean
      get() = _syncingFlow.value
      set(value) {
         _syncingFlow.value = value
      }

   override val isSyncing: Boolean
      get() = _syncing

   override val syncingFlow: Flow<Boolean>
      get() = _syncingFlow.asStateFlow()

   override suspend fun syncOrThrow(): T {
      return syncWithResult().getOrThrow()
   }

   override suspend fun syncWithResult(): Result<T> {
      if (currentCoroutineContext()[SyncElement]?.syncable === this@SyncableImpl) {
         throw ReSyncException("Can not call sync in the onSync block.")
      }
      return withContext(Dispatchers.fMain) {
         if (_syncing) {
            _continuations.await()
         } else {
            runCatching {
               startSync()
            }.onSuccess { data ->
               _continuations.resumeAll(Result.success(data))
            }.onFailure { error ->
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
      return try {
         _syncing = true
         withContext(SyncElement(this@SyncableImpl)) {
            onSync()
         }.also {
            currentCoroutineContext().ensureActive()
         }
      } finally {
         _syncing = false
      }
   }
}

private class SyncElement(
   val syncable: FSyncable<*>,
) : AbstractCoroutineContextElement(SyncElement) {
   companion object Key : CoroutineContext.Key<SyncElement>
}

private class ReSyncException(message: String) : RuntimeException(message)