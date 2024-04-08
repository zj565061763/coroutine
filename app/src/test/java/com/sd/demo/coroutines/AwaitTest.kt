package com.sd.demo.coroutines

import com.sd.lib.coroutines.fAwait
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.coroutines.resume

class AwaitTest {
    @Test
    fun `test resume`(): Unit = runBlocking {
        val result = fAwait { cont ->
            cont.resume(1)
            cont.resume(2)
        }
        assertEquals(1, result)
    }
}