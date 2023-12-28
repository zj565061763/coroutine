package com.sd.lib.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.WeakHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FScope(scope: CoroutineScope = MainScope()) {
    private val _scope = scope
    private val _jobHolder: MutableMap<Job, String> = Collections.synchronizedMap(WeakHashMap())

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
            _jobHolder[job] = ""
            job.invokeOnCompletion {
                _jobHolder.remove(job)
            }
        }
    }

    /**
     * 取消协程
     */
    fun cancel() {
        while (_jobHolder.isNotEmpty()) {
            _jobHolder.keys.toMutableList().forEach { job ->
                job.cancel()
                _jobHolder.remove(job)
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