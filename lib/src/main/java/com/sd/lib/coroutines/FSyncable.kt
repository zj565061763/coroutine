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

    private val _continuation = object : FContinuation<Result<T>>() {
        override fun onFirstAwait() {
            startSync()
        }
    }

    override fun sync() {
        startSync()
    }

    override suspend fun syncAwait(): Result<T> {
        return _continuation.await()
    }

    private fun startSync() {
        scope.launch {
            if (_syncFlag.compareAndSet(false, true)) {
                try {
                    val data = onSync()
                    currentCoroutineContext().ensureActive()
                    _continuation.resume(Result.success(data))
                } catch (e: Throwable) {
                    if (e is CancellationException) {
                        _continuation.cancel()
                        throw e
                    } else {
                        _continuation.resume(Result.failure(e))
                    }
                } finally {
                    _syncFlag.set(false)
                }
            }
        }

        if (!scope.isActive) {
            _continuation.cancel()
        }
    }
}