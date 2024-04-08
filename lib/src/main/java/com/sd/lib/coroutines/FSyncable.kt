package com.sd.lib.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

interface FSyncable<T> {
    /**
     * 开始同步
     */
    fun sync()

    /**
     * 开始同步并等待结果
     */
    suspend fun syncAwait(): Result<T>
}

/**
 * 创建[FSyncable]，当[FSyncable.sync]或者[FSyncable.syncAwait]触发时，回调[onSync]进行同步操作。
 * [onSync]在[scope]上面执行，执行完成后会唤醒[FSyncable.syncAwait]挂起的协程，
 * 如果[onSync]或者[scope]被取消，则[FSyncable.syncAwait]挂起的协程会被取消。
 */
fun <T> FSyncable(
    scope: CoroutineScope = MainScope(),
    onSync: suspend () -> T,
): FSyncable<T> {
    return SyncableImpl(
        scope = scope,
        onSync = onSync,
    )
}

private class SyncableImpl<T>(
    private val scope: CoroutineScope,
    private val onSync: suspend () -> T,
) : FSyncable<T> {

    private val _syncFlag = AtomicBoolean(false)

    private val _continuations = object : FContinuations<Result<T>>() {
        override fun onFirstAwait() {
            startSync()
        }
    }

    override fun sync() {
        startSync()
    }

    override suspend fun syncAwait(): Result<T> {
        return _continuations.await()
    }

    private fun startSync() {
        scope.launch {
            if (_syncFlag.compareAndSet(false, true)) {
                try {
                    val data = onSync()
                    currentCoroutineContext().ensureActive()
                    _continuations.resumeAll(Result.success(data))
                } catch (e: Throwable) {
                    if (e is CancellationException) {
                        _continuations.cancelAll()
                        throw e
                    } else {
                        _continuations.resumeAll(Result.failure(e))
                    }
                } finally {
                    _syncFlag.set(false)
                }
            }
        }

        if (!scope.isActive) {
            _continuations.cancelAll()
        }
    }
}