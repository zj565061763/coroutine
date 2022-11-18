package com.sd.lib.coroutine

import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FScope(scope: CoroutineScope = MainScope()) {
    private val _scope = scope
    private var _jobHolder: MutableMap<Job, String>? = null

    /**
     * 启动协程
     */
    @Synchronized
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
            val holder = _jobHolder ?: WeakHashMap<Job, String>().also { map ->
                _jobHolder = map
            }
            holder[job] = ""
        }
    }

    /**
     * 取消协程
     */
    @Synchronized
    fun cancel() {
        _jobHolder?.let { holder ->
            holder.keys.forEach { it.cancel() }
            _jobHolder = null
        }
    }
}