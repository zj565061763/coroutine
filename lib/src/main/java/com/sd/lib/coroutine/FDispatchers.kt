package com.sd.lib.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

object FDispatchers {
    @OptIn(ExperimentalCoroutinesApi::class)
    val SingleIO = Dispatchers.IO.limitedParallelism(1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val SingleDefault = Dispatchers.Default.limitedParallelism(1)
}