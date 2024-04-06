package com.sd.lib.coroutine

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

open class FContinuation<T> {
    private val _holder: MutableSet<CancellableContinuation<T>> = mutableSetOf()

    suspend fun await(): T {
        return suspendCancellableCoroutine { cont ->
            addContinuation(cont)
            cont.invokeOnCancellation {
                removeContinuation(cont)
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
            if (oldSize == 0) onFirstAwait()
        }
    }

    @Synchronized
    private fun removeContinuation(cont: CancellableContinuation<T>) {
        _holder.remove(cont)
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
     * [await]的数量从0到1时触发
     */
    protected open fun onFirstAwait() = Unit
}