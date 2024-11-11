package com.sd.lib.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface FLoader {

   /** 状态流 */
   val stateFlow: Flow<LoaderState>

   /** 加载状态流 */
   val loadingFlow: Flow<Boolean>

   /** 状态 */
   val state: LoaderState

   /** 是否正在加载中 */
   val isLoading: Boolean

   /**
    * 开始加载，如果上一次加载还未完成，再次调用此方法，会取消上一次加载([CancellationException])，
    * 如果[onLoad]触发了，则[onFinish]一定会触发，[onLoad]的异常会被捕获，除了[CancellationException]
    *
    * @param notifyLoading 是否通知加载状态
    * @param onFinish 结束回调
    * @param onLoad 加载回调
    */
   suspend fun <T> load(
      notifyLoading: Boolean? = null,
      onFinish: () -> Unit = {},
      onLoad: suspend () -> T,
   ): Result<T>

   /**
    * 取消加载
    */
   suspend fun cancelLoad()
}

/**
 * 创建[FLoader]
 */
fun FLoader(
   notifyLoading: () -> Boolean = { true },
): FLoader = LoaderImpl(notifyLoading = notifyLoading)

//-------------------- state --------------------

data class LoaderState(
   /** 是否正在加载中 */
   val isLoading: Boolean = false,

   /** 最后一次的加载结果 */
   val result: Result<Unit>? = null,
)

//-------------------- impl --------------------

private class LoaderImpl(
   private val notifyLoading: () -> Boolean,
) : FLoader {
   private val _mutator = FMutator()

   private val _stateFlow = MutableStateFlow(LoaderState())
   override val stateFlow: Flow<LoaderState> = _stateFlow.asStateFlow()

   override val loadingFlow: Flow<Boolean>
      get() = stateFlow.map { it.isLoading }.distinctUntilChanged()

   override val state: LoaderState
      get() = _stateFlow.value

   override val isLoading: Boolean
      get() = state.isLoading

   override suspend fun <T> load(
      notifyLoading: Boolean?,
      onFinish: () -> Unit,
      onLoad: suspend () -> T,
   ): Result<T> {
      return _mutator.mutate {
         val loading = notifyLoading ?: this.notifyLoading()
         currentCoroutineContext().ensureActive()
         try {
            if (loading) {
               _stateFlow.update { it.copy(isLoading = true) }
            }
            onLoad().let { data ->
               currentCoroutineContext().ensureActive()
               Result.success(data).also {
                  _stateFlow.update { it.copy(result = Result.success(Unit)) }
               }
            }
         } catch (e: Throwable) {
            if (e is CancellationException) throw e
            Result.failure<T>(e).also {
               _stateFlow.update { it.copy(result = Result.failure(e)) }
            }
         } finally {
            if (loading) {
               _stateFlow.update { it.copy(isLoading = false) }
            }
            onFinish()
         }
      }
   }

   override suspend fun cancelLoad() {
      _mutator.cancelAndJoin()
   }
}