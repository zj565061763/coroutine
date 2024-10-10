package com.sd.lib.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Collections
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FContinuations<T> {
   private val _holder: MutableSet<CancellableContinuation<T>> = Collections.synchronizedSet(mutableSetOf())

   /**
    * 挂起当前协程，等待结果[T]
    */
   suspend fun await(): T {
      return suspendCancellableCoroutine { cont ->
         _holder.add(cont)
         cont.invokeOnCancellation {
            _holder.remove(cont)
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

   private inline fun foreach(block: (CancellableContinuation<T>) -> Unit) {
      while (_holder.isNotEmpty()) {
         _holder.toTypedArray().forEach { cont ->
            _holder.remove(cont)
            block(cont)
         }
      }
   }
}