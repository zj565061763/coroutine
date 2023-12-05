package com.sd.lib.coroutine

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Collections
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FContinuation<T> {
    private val _continuationHolder: MutableList<CancellableContinuation<T>> = Collections.synchronizedList(mutableListOf())

    suspend fun await(onCancel: CompletionHandler? = null): T {
        return suspendCancellableCoroutine { cont ->
            _continuationHolder.add(cont)
            cont.invokeOnCancellation {
                _continuationHolder.remove(cont)
                onCancel?.invoke(it)
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
        return _continuationHolder.size
    }

    private fun foreach(
        remove: Boolean = true,
        block: (CancellableContinuation<T>) -> Unit,
    ) {
        while (_continuationHolder.isNotEmpty()) {
            _continuationHolder.toMutableList().forEach {
                try {
                    block(it)
                } finally {
                    if (remove) {
                        _continuationHolder.remove(it)
                    }
                }
            }
        }
    }
}