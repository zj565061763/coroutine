package com.sd.lib.coroutine

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Collections
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FContinuation<T> {
    private val _holder: MutableList<CancellableContinuation<T>> = Collections.synchronizedList(mutableListOf())

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
            _holder.toMutableList().forEach {
                try {
                    block(it)
                } finally {
                    if (remove) {
                        _holder.remove(it)
                    }
                }
            }
        }
    }
}