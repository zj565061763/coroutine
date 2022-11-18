package com.sd.lib.coroutine

import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FScope(scope: CoroutineScope = MainScope()) {
    private val _scope = scope
    private val _jobHolder: MutableMap<Job, String> = WeakHashMap()

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
        ).also {
            _jobHolder[it] = ""
        }
    }

    /**
     * 取消协程
     */
    @Synchronized
    fun cancel() {
        _jobHolder.keys.forEach { it.cancel() }
        _jobHolder.clear()
    }
}