package com.sd.lib.coroutine

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Collections
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FContinuation<T> {
    private val _holder: MutableSet<CancellableContinuation<T>> = Collections.synchronizedSet(hashSetOf())

    suspend fun await(): T {
        return suspendCancellableCoroutine { cont ->
            _holder.add(cont)
            cont.invokeOnCancellation {
                _holder.remove(cont)
            }
        }
    }

    fun resume(value: T) {
        foreach { cont ->
            cont.resume(value)
        }
    }

    fun resumeWithException(exception: Throwable) {
        foreach { cont ->
            cont.resumeWithException(exception)
        }
    }

    fun cancel(cause: Throwable? = null) {
        foreach { cont ->
            cont.cancel(cause)
        }
    }

    private fun foreach(block: (CancellableContinuation<T>) -> Unit) {
        while (_holder.isNotEmpty()) {
            _holder.toTypedArray().forEach { cont ->
                _holder.remove(cont)
                block(cont)
            }
        }
    }
}