package com.sd.lib.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class FMutableFlow<T, F : MutableSharedFlow<T>> {
    private val _flows: MutableMap<String, F> = hashMapOf()
    private var _scope: CoroutineScope? = null

    /**
     * 获取[key]对应的[MutableSharedFlow]，当[MutableSharedFlow.subscriptionCount]为0时，会移除该Flow
     */
    fun get(
        key: String,
        factory: () -> F,
    ): F {
        synchronized(this@FMutableFlow) {
            return _flows.getOrPut(key) { createFlowLocked(key, factory) }
        }
    }

    /**
     * 清空所有保存的[MutableSharedFlow]
     */
    fun clear() {
        synchronized(this@FMutableFlow) {
            _flows.clear()
            _scope?.cancel()
            _scope = null
        }
    }

    /**
     * 当前保存的[MutableSharedFlow]数量
     */
    fun size(): Int {
        synchronized(this@FMutableFlow) {
            return _flows.size
        }
    }

    private fun createFlowLocked(
        key: String,
        factory: () -> F,
    ): F {
        return factory().also { flow ->
            launchLocked {
                delay(1000)
                val context = currentCoroutineContext()
                flow.subscriptionCount.collect { count ->
                    if (count > 0) {
                        // active
                    } else {
                        context.cancel()
                    }
                }
            }.let { job ->
                job.invokeOnCompletion {
                    synchronized(this@FMutableFlow) {
                        _flows.remove(key)
                    }
                }
            }
        }
    }

    private fun launchLocked(block: suspend CoroutineScope.() -> Unit): Job {
        val scope = _scope ?: MainScope().also {
            _scope = it
        }
        return scope.launch(block = block)
    }
}