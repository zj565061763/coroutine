package com.sd.lib.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class FMutableFlowStore<T : MutableSharedFlow<*>> {
    private val _flows: MutableMap<Any, T> = hashMapOf()
    private var _scope: CoroutineScope? = null

    /**
     * 获取[key]对应的[MutableSharedFlow]，当[MutableSharedFlow.subscriptionCount]为0时，会移除该Flow
     */
    fun get(
        key: Any,
        factory: () -> T,
    ): T {
        synchronized(this@FMutableFlowStore) {
            return _flows.getOrPut(key) { createFlowLocked(key, factory) }
        }
    }

    /**
     * 清空所有保存的[MutableSharedFlow]
     */
    fun clear() {
        synchronized(this@FMutableFlowStore) {
            _flows.clear()
            _scope?.cancel()
            _scope = null
        }
    }

    /**
     * 当前保存的[MutableSharedFlow]数量
     */
    fun size(): Int {
        synchronized(this@FMutableFlowStore) {
            return _flows.size
        }
    }

    private fun createFlowLocked(
        key: Any,
        factory: () -> T,
    ): T {
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
                    synchronized(this@FMutableFlowStore) {
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