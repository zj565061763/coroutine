package com.sd.lib.coroutine

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FContinuation<T> {
    private val _continuationHolder: MutableSet<CancellableContinuation<T>> = mutableSetOf()

    suspend fun await(): T {
        return suspendCancellableCoroutine { cont ->
            synchronized(this@FContinuation) {
                _continuationHolder.add(cont)
            }

            cont.invokeOnCancellation {
                synchronized(this@FContinuation) {
                    _continuationHolder.remove(cont)
                }
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
        foreach {
            it.cancel(cause)
        }
    }

    @Synchronized
    fun size(): Int {
        return _continuationHolder.size
    }

    @Synchronized
    private fun foreach(block: (CancellableContinuation<T>) -> Unit) {
        while (_continuationHolder.isNotEmpty()) {
            val copyHolder = _continuationHolder.toSet()
            copyHolder.forEach {
                try {
                    block(it)
                } finally {
                    _continuationHolder.remove(it)
                }
            }
        }
    }
}