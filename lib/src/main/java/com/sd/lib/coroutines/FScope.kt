package com.sd.lib.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
     * 取消协程，[FScope]不会被取消
     */
    fun cancel()
}

/**
 * 创建[FScope]
 */
fun FScope(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
): FScope {
    return ScopeImpl(scope = scope)
}

private open class ScopeImpl(
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

/**
 * 全局协程作用域，所有创建的协程只能通过对应的[Job]手动取消，
 * 不能调用[FScope.cancel]取消，否则会抛异常
 */
val fGlobalScope: FScope = object : ScopeImpl(
    scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) {
    override fun cancel() {
        error("Can not cancel global scope.")
    }
}