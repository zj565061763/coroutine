package com.sd.lib.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface FScope {
   /**
    * 启动协程
    */
   fun launch(
      context: CoroutineContext = EmptyCoroutineContext,
      start: CoroutineStart = CoroutineStart.DEFAULT,
      block: suspend CoroutineScope.() -> Unit,
   ): Job

   /**
    * 取消[launch]启动的协程
    */
   fun cancel()
}

/**
 * 创建[FScope]
 */
fun FScope(
   scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.fMain),
): FScope = ScopeImpl(scope = scope)

private open class ScopeImpl(
   private val scope: CoroutineScope,
) : FScope {
   private val _holder: MutableSet<Job> = mutableSetOf()

   override fun launch(
      context: CoroutineContext,
      start: CoroutineStart,
      block: suspend CoroutineScope.() -> Unit,
   ): Job {
      return scope.launch(
         context = context,
         start = start,
         block = block,
      ).also { job ->
         synchronized(this@ScopeImpl) {
            _holder.add(job)
         }
         job.invokeOnCompletion {
            synchronized(this@ScopeImpl) {
               _holder.remove(job)
            }
         }
      }
   }

   override fun cancel() {
      synchronized(this@ScopeImpl) {
         _holder.toTypedArray().also {
            _holder.clear()
         }
      }.forEach { it.cancel() }
   }
}

/**
 * 全局协程作用域，所有创建的协程只能通过对应的[Job]手动取消，
 * 不能调用[FScope.cancel]取消，否则会抛异常
 */
val fGlobalScope: FScope = object : ScopeImpl(
   scope = CoroutineScope(SupervisorJob() + Dispatchers.fMain)
) {
   override fun cancel() {
      error("Can not cancel global scope.")
   }
}

val Dispatchers.fMain: MainCoroutineDispatcher
   get() = runCatching { Main.immediate }.getOrDefault(Main)