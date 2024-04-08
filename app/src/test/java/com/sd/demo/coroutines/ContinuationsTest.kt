package com.sd.demo.coroutines

import com.sd.lib.coroutines.FContinuations
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ContinuationsTest {
    @Test
    fun `test resume`(): Unit = runBlocking {
        val continuations = FContinuations<Int>()

        val count = AtomicInteger(0)
        val jobs = mutableSetOf<Job>()

        repeat(5) {
            launch {
                val result = continuations.await()
                count.updateAndGet { it + result }
            }.also { job ->
                jobs.add(job)
            }
        }

        assertEquals(5, jobs.size)
        delay(1_000)

        // resume
        continuations.resume(1)
        continuations.resume(2)

        jobs.joinAll()
        jobs.forEach { assertEquals(true, it.isCompleted) }
        assertEquals(5, count.get())
    }

    @Test
    fun `test resumeWithException`(): Unit = runBlocking {
        val continuations = FContinuations<Int>()

        val count = AtomicInteger(0)
        val jobs = mutableSetOf<Job>()

        repeat(5) {
            launch {
                val result = try {
                    continuations.await()
                } catch (e: Exception) {
                    assertEquals("resumeWithException1", e.message)
                    0
                }
                count.updateAndGet { it + result }
            }.also { job ->
                jobs.add(job)
            }
        }

        assertEquals(5, jobs.size)
        delay(1_000)

        // resumeWithException
        continuations.resumeWithException(Exception("resumeWithException1"))
        continuations.resumeWithException(Exception("resumeWithException2"))

        jobs.joinAll()
        jobs.forEach { assertEquals(true, it.isCompleted) }
        assertEquals(0, count.get())
    }

    @Test
    fun `test cancel outside`(): Unit = runBlocking {
        val continuations = FContinuations<Int>()

        val count = AtomicInteger(0)
        val jobs = mutableSetOf<Job>()

        val repeat = 5
        repeat(repeat) {
            launch {
                val result = continuations.await()
                count.updateAndGet { it + result }
            }.also { job ->
                jobs.add(job)
            }
        }

        assertEquals(repeat, jobs.size)
        delay(1_000)

        // cancel outside
        jobs.forEach { it.cancelAndJoin() }
        jobs.forEach { assertEquals(true, it.isCancelled) }
        assertEquals(0, count.get())
    }

    @Test
    fun `test cancel inside`(): Unit = runBlocking {
        val continuations = FContinuations<Int>()

        val count = AtomicInteger(0)
        val jobs = mutableSetOf<Job>()

        val repeat = 5
        repeat(repeat) {
            launch {
                val result = continuations.await()
                count.updateAndGet { it + result }
            }.also { job ->
                jobs.add(job)
            }
        }

        assertEquals(repeat, jobs.size)
        delay(1_000)

        // cancel inside
        continuations.cancel()

        jobs.joinAll()
        jobs.forEach { assertEquals(true, it.isCancelled) }
        assertEquals(0, count.get())
    }

    @Test
    fun `test cancel inside with cause`(): Unit = runBlocking {
        val continuations = FContinuations<Int>()

        val count = AtomicInteger(0)
        val jobs = mutableSetOf<Job>()

        val repeat = 5
        repeat(repeat) {
            launch {
                val result = try {
                    continuations.await()
                } catch (e: Exception) {
                    assertEquals("cancel with cause", e.message)
                    0
                }
                count.updateAndGet { it + result }
            }.also { job ->
                jobs.add(job)
            }
        }

        assertEquals(repeat, jobs.size)
        delay(1_000)

        // cancel inside with cause
        continuations.cancel(Exception("cancel with cause"))

        jobs.joinAll()
        jobs.forEach { assertEquals(true, it.isCompleted) }
        assertEquals(0, count.get())
    }

    @Test
    fun `test onFirstAwait`(): Unit = runBlocking {
        val count = AtomicInteger(0)
        val continuations = object : FContinuations<Unit>() {
            override fun onFirstAwait() {
                count.incrementAndGet()
            }
        }

        mutableSetOf<Job>().let { jobs ->
            val repeat = 5
            repeat(repeat) {
                launch {
                    continuations.await()
                }.also { job ->
                    jobs.add(job)
                }
            }
            assertEquals(repeat, jobs.size)
            delay(1_000)
            continuations.resume(Unit)
            jobs.joinAll()
            assertEquals(1, count.get())
        }

        mutableSetOf<Job>().let { jobs ->
            val repeat = 5
            repeat(repeat) {
                launch {
                    continuations.await()
                }.also { job ->
                    jobs.add(job)
                }
            }
            assertEquals(repeat, jobs.size)
            delay(1_000)
            continuations.resume(Unit)
            jobs.joinAll()
            assertEquals(2, count.get())
        }
    }
}