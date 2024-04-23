package com.sd.lib.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 挂起当前协程，执行[block]
 */
suspend fun <T> fAwait(
    onError: (Throwable) -> Unit = { it.printStackTrace() },
    block: (CancellableContinuation<T>) -> Unit,
): T = suspendCancellableCoroutine { continuation ->
    block(
        SafeCancellableContinuation(
            onError = onError,
            continuation = continuation,
        )
    )
}

private class SafeCancellableContinuation<T>(
    private val onError: (Throwable) -> Unit,
    private val continuation: CancellableContinuation<T>,
) : CancellableContinuation<T> by continuation {
    override fun resumeWith(result: Result<T>) {
        try {
            continuation.resumeWith(result)
        } catch (e: IllegalStateException) {
            onError(e)
        }
    }
}