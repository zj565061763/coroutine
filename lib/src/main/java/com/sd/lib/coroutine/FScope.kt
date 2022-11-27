package com.sd.lib.coroutine

import kotlinx.coroutines.*
import java.util.*
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
        }
    }

    /**
     * 取消协程
     */
    fun cancel() {
        while (_jobHolder.isNotEmpty()) {
            val copyKeys = _jobHolder.keys.toHashSet()
            copyKeys.forEach { job ->
                try {
                    job.cancel()
                } finally {
                    _jobHolder.remove(job)
                }
            }
        }
    }
}