package com.sd.lib.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun <T> fAwait(
    block: (CancellableContinuation<T>) -> Unit
): T = suspendCancellableCoroutine { continuation ->
    block(SafeCancellableContinuation(continuation))
}

private class SafeCancellableContinuation<T>(
    private val continuation: CancellableContinuation<T>
) : CancellableContinuation<T> by continuation {
    override fun resumeWith(result: Result<T>) {
        try {
            continuation.resumeWith(result)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }
}