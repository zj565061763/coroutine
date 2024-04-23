package com.sd.lib.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

open class FContinuations<T> {
    private val _holder: MutableSet<CancellableContinuation<T>> = mutableSetOf()

    /**
     * 挂起当前协程，等待结果[T]
     */
    suspend fun await(): T {
        return suspendCancellableCoroutine { cont ->
            synchronized(this@FContinuations) {
                if (_holder.add(cont) && _holder.size == 1) {
                    onFirstAwait()
                }
            }
            cont.invokeOnCancellation {
                synchronized(this@FContinuations) {
                    _holder.remove(cont)
                }
            }
        }
    }

    /**
     * 恢复所有挂起的协程
     */
    fun resumeAll(value: T) {
        foreach {
            it.resume(value)
        }
    }

    /**
     * 恢复所有挂起的协程，并在挂起点抛出异常[exception]
     */
    fun resumeAllWithException(exception: Throwable) {
        foreach {
            it.resumeWithException(exception)
        }
    }

    /**
     * 取消所有挂起的协程
     */
    fun cancelAll(cause: Throwable? = null) {
        foreach {
            it.cancel(cause)
        }
    }

    private fun foreach(block: (CancellableContinuation<T>) -> Unit) {
        synchronized(this@FContinuations) {
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