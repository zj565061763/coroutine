package com.sd.lib.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FScope(
    private val scope: CoroutineScope = MainScope()
) {
    private val _holder: MutableSet<Job> = Collections.synchronizedSet(mutableSetOf())

    /**
     * 启动协程
     */
    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
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

    /**
     * 取消协程，[scope]不会被取消
     */
    fun cancel() {
        while (_holder.isNotEmpty()) {
            _holder.toTypedArray().forEach { job ->
                _holder.remove(job)
                job.cancel()
            }
        }
    }
}