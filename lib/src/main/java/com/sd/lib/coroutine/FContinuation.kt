package com.sd.lib.coroutine

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

open class FContinuation<T> {
    private val _holder: MutableSet<CancellableContinuation<T>> = mutableSetOf()

    suspend fun await(): T {
        return suspendCancellableCoroutine { cont ->
            synchronized(this@FContinuation) {
                if (_holder.add(cont)) {
                    if (_holder.size == 1 && cont.isActive) {
                        onFirstAwait()
                    }
                }
            }
            cont.invokeOnCancellation {
                synchronized(this@FContinuation) {
                    _holder.remove(cont)
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

    private fun foreach(block: (CancellableContinuation<T>) -> Unit) {
        synchronized(this@FContinuation) {
            while (_holder.isNotEmpty()) {
                _holder.toTypedArray().forEach { cont ->
                    _holder.remove(cont)
                    block(cont)
                }
            }
        }
    }

    /**
     * [await]保存的[CancellableContinuation]数量从0到1时触发
     */
    protected open fun onFirstAwait() = Unit
}