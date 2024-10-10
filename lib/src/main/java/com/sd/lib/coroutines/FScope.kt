package com.sd.lib.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Collections
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
   scope: CoroutineScope = fMainScope(),
): FScope = ScopeImpl(scope = scope)

private class ScopeImpl(
   private val scope: CoroutineScope,
) : FScope {
   private val _holder: MutableSet<Job> = Collections.synchronizedSet(mutableSetOf())

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
         _holder.add(job)
         job.invokeOnCompletion {
            _holder.remove(job)
         }
      }
   }

   override fun cancel() {
      while (_holder.isNotEmpty()) {
         _holder.toTypedArray().forEach { job ->
            _holder.remove(job)
            job.cancel()
         }
      }
   }
}

fun fMainScope(): CoroutineScope {
   return CoroutineScope(SupervisorJob() + Dispatchers.fMain)
}

val Dispatchers.fMain: MainCoroutineDispatcher
   get() = runCatching { Main.immediate }.getOrDefault(Main)