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
    private val _jobHolder: MutableMap<Job, String> = Collections.synchronizedMap(hashMapOf())

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
            job.invokeOnCompletion {
                _jobHolder.remove(job)
            }
            if (job.isActive) {
                _jobHolder[job] = ""
            }
        }
    }

    /**
     * 取消协程
     */
    fun cancel() {
        while (_jobHolder.isNotEmpty()) {
            _jobHolder.keys.toMutableList().forEach { job ->
                _jobHolder.remove(job)
                job.cancel()
            }
        }
    }

    /**
     * 通过[launch]启动的存活协程数量
     */
    fun size(): Int {
        return _jobHolder.size
    }
}