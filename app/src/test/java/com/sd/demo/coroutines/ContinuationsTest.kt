package com.sd.demo.coroutines

import com.sd.lib.coroutines.FContinuations
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ContinuationsTest {
    @Test
    fun `test resumeAll`(): Unit = runBlocking {
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

        // resumeAll
        continuations.resumeAll(1)
        continuations.resumeAll(2)

        jobs.joinAll()
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
                } catch (e: Throwable) {
                    assertEquals("resumeWithException1", e.message)
                    1
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
        assertEquals(5, count.get())
    }

    @Test
    fun `test cancel`(): Unit = runBlocking {
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

        // cancel
        continuations.cancel()
        continuations.cancel()

        jobs.joinAll()
        jobs.forEach { assertEquals(true, it.isCancelled) }
        assertEquals(0, count.get())
    }

    @Test
    fun `test cancel with cause`(): Unit = runBlocking {
        val continuations = FContinuations<Int>()

        val count = AtomicInteger(0)
        val jobs = mutableSetOf<Job>()

        val repeat = 5
        repeat(repeat) {
            launch {
                val result = try {
                    continuations.await()
                } catch (e: Throwable) {
                    assertEquals("cancel with cause 1", e.message)
                    1
                }
                count.updateAndGet { it + result }
            }.also { job ->
                jobs.add(job)
            }
        }

        assertEquals(repeat, jobs.size)
        delay(1_000)

        // cancel with cause
        continuations.cancel(Exception("cancel with cause 1"))
        continuations.cancel(Exception("cancel with cause 2"))

        jobs.joinAll()
        assertEquals(5, count.get())
    }

    @Test
    fun `test cancel outside`(): Unit = runBlocking {
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

        // cancel outside
        jobs.forEach { it.cancel() }

        jobs.joinAll()
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
            repeat(5) {
                launch {
                    continuations.await()
                }.also { job ->
                    jobs.add(job)
                }
            }
            assertEquals(5, jobs.size)
            delay(1_000)

            continuations.resumeAll(Unit)
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

            continuations.resumeAll(Unit)
            jobs.joinAll()
            assertEquals(2, count.get())
        }
    }

    @Test
    fun `test onFirstAwait resume`(): Unit = runBlocking {
        val continuations = object : FContinuations<Int>() {
            override fun onFirstAwait() {
                // resume
                resumeAll(1)
                resumeAll(2)
            }
        }
        assertEquals(1, continuations.await())
    }

    @Test
    fun `test onFirstAwait resumeWithException`(): Unit = runBlocking {
        val continuations = object : FContinuations<Int>() {
            override fun onFirstAwait() {
                // resumeWithException
                resumeWithException(Exception("resumeWithException1"))
                resumeWithException(Exception("resumeWithException2"))
            }
        }

        val result = try {
            continuations.await()
        } catch (e: Throwable) {
            assertEquals("resumeWithException1", e.message)
            1
        }

        assertEquals(1, result)
    }

    @Test
    fun `test onFirstAwait cancel`(): Unit = runBlocking {
        val continuations = object : FContinuations<Int>() {
            override fun onFirstAwait() {
                // cancel
                cancel()
            }
        }

        val result = try {
            continuations.await()
        } catch (e: CancellationException) {
            1
        }

        assertEquals(1, result)
    }
}