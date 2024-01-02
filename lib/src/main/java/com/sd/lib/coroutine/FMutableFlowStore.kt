package com.sd.lib.coroutine

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class FMutableFlowStore<T : MutableSharedFlow<*>> {
    private val _holder: MutableMap<Any, T> = hashMapOf()
    private val _scope = MainScope()

    /**
     * 获取[key]对应的[MutableSharedFlow]
     */
    fun get(key: Any): T? {
        synchronized(this@FMutableFlowStore) {
            return _holder[key]
        }
    }

    /**
     * 获取[key]对应的[MutableSharedFlow]，如果不存在则调用[factory]创建并保存，
     * 当[MutableSharedFlow.subscriptionCount]等于0时，会移除该对象
     */
    fun getOrPut(
        key: Any,
        factory: () -> T,
    ): T {
        synchronized(this@FMutableFlowStore) {
            return _holder.getOrPut(key) { createFlowLocked(key, factory) }
        }
    }

    /**
     * 当前保存的[MutableSharedFlow]数量
     */
    fun size(): Int {
        synchronized(this@FMutableFlowStore) {
            return _holder.size
        }
    }

    private fun createFlowLocked(
        key: Any,
        factory: () -> T,
    ): T {
        return factory().also { flow ->
            _scope.launch {
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
                        _holder.remove(key)
                    }
                }
            }
        }
    }
}