package com.sd.lib.coroutine

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Collections
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FContinuation<T> {
    private val _holder: MutableSet<CancellableContinuation<T>> = Collections.synchronizedSet(hashSetOf())

    suspend fun await(onCancel: CompletionHandler? = null): T {
        return suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation {
                _holder.remove(cont)
                onCancel?.invoke(it)
            }
            if (cont.isActive) {
                _holder.add(cont)
            }
        }
    }

    fun resume(value: T) {
        foreach {
            it.resume(value)
        }
    }

    fun resumeWithException(exception: Throwable) {
        foreach {
            it.resumeWithException(exception)
        }
    }

    fun cancel(cause: Throwable? = null) {
        foreach(remove = false) {
            it.cancel(cause)
        }
    }

    fun size(): Int {
        return _holder.size
    }

    private fun foreach(
        remove: Boolean = true,
        block: (CancellableContinuation<T>) -> Unit,
    ) {
        while (_holder.isNotEmpty()) {
            _holder.toTypedArray().forEach { cont ->
                if (remove) _holder.remove(cont)
                block(cont)
            }
        }
    }
}