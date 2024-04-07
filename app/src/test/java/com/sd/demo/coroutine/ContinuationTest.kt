package com.sd.demo.coroutine

import com.sd.lib.coroutine.FContinuation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ContinuationTest {
    @Test
    fun `test resume`(): Unit = runBlocking {
        val continuation = FContinuation<Int>()

        val count = AtomicInteger(0)
        val jobs = mutableSetOf<Job>()

        repeat(5) {
            launch {
                val result = continuation.await()
                count.updateAndGet { it + result }
            }.also { job ->
                jobs.add(job)
            }
        }

        delay(1_000)
        continuation.resume(1)

        jobs.joinAll()
        assertEquals(5, count.get())
    }
}