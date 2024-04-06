package com.sd.lib.coroutine

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

open class FContinuation<T> {
    private val _holder: MutableSet<CancellableContinuation<T>> = mutableSetOf()

    suspend fun await(onCancel: CompletionHandler? = null): T {
        return suspendCancellableCoroutine { cont ->
            addContinuation(cont)
            cont.invokeOnCancellation {
                try {
                    onCancel?.invoke(it)
                } finally {
                    removeContinuation(cont)
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
    private fun addContinuation(cont: CancellableContinuation<T>) {
        val oldSize = _holder.size
        if (_holder.add(cont)) {
            onSizeChange(oldSize, _holder.size)
        }
    }

    @Synchronized
    private fun removeContinuation(cont: CancellableContinuation<T>) {
        val oldSize = _holder.size
        if (_holder.remove(cont)) {
            onSizeChange(oldSize, _holder.size)
        }
    }

    @Synchronized
    private fun foreach(block: (CancellableContinuation<T>) -> Unit) {
        while (_holder.isNotEmpty()) {
            _holder.toTypedArray().forEach { cont ->
                block(cont)
                removeContinuation(cont)
            }
        }
    }

    /**
     * 数量变化回调
     */
    protected open fun onSizeChange(oldSize: Int, newSize: Int) = Unit
}