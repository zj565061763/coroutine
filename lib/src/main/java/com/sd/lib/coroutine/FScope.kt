package com.sd.lib.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FScope(scope: CoroutineScope = MainScope()) {
    private val _scope = scope
    private val _holder: MutableSet<Job> = Collections.synchronizedSet(hashSetOf())

    /**
     * 启动协程
     */
    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit,
    ): Job {
        return _scope.launch(
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
     * 取消协程
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