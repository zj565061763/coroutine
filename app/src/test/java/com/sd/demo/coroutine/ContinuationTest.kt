package com.sd.demo.coroutine

import com.sd.lib.coroutine.FContinuation
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
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
        jobs.forEach { assertEquals(true, it.isCompleted) }
        assertEquals(5, count.get())
    }

    @Test
    fun `test resumeWithException`(): Unit = runBlocking {
        val continuation = FContinuation<Int>()

        val count = AtomicInteger(0)
        val jobs = mutableSetOf<Job>()

        repeat(5) {
            launch {
                val result = try {
                    continuation.await()
                } catch (e: Exception) {
                    assertEquals("resumeWithException", e.message)
                    0
                }
                count.updateAndGet { it + result }
            }.also { job ->
                jobs.add(job)
            }
        }

        delay(1_000)
        continuation.resumeWithException(Exception("resumeWithException"))

        jobs.joinAll()
        jobs.forEach { assertEquals(true, it.isCompleted) }
        assertEquals(0, count.get())
    }

    @Test
    fun `test cancel out`(): Unit = runBlocking {
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
        jobs.forEach { it.cancelAndJoin() }
        jobs.forEach { assertEquals(true, it.isCancelled) }
        assertEquals(0, count.get())
    }
}