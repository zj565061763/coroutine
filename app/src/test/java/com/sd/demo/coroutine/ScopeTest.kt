package com.sd.demo.coroutine

import com.sd.lib.coroutine.FScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ScopeTest {
    @Test
    fun `test launch size`(): Unit = runBlocking {
        val scope = FScope(this)

        val count = AtomicInteger(0)
        val jobs = mutableSetOf<Job>()

        val repeat = 5
        repeat(repeat) {
            scope.launch {
                delay(2_000)
                count.incrementAndGet()
            }.let { job ->
                jobs.add(job)
            }
        }

        assertEquals(repeat, jobs.size)
        delay(1_000)
        jobs.forEach { assertEquals(true, it.isActive) }
        assertEquals(repeat, scope.size())

        jobs.joinAll()
        assertEquals(repeat, count.get())
    }

    @Test
    fun `test cancel scope`(): Unit = runBlocking {
        val scope = FScope(this)

        scope.launch {
            delay(Long.MAX_VALUE)
        }.let { job ->
            delay(1_000)
            assertEquals(true, job.isActive)
            scope.cancel()
            assertEquals(true, job.isCancelled)
            job.join()
        }

        val count = AtomicInteger(0)
        scope.launch {
            delay(2_000)
            count.incrementAndGet()
        }.let { job ->
            delay(1_000)
            assertEquals(true, job.isActive)
            job.join()
            assertEquals(1, count.get())
        }
    }

    @Test
    fun `test cancel out scope`(): Unit = runBlocking {
        val outScope = CoroutineScope(SupervisorJob())
        val scope = FScope(outScope)

        scope.launch {
            delay(Long.MAX_VALUE)
        }.let { job ->
            delay(1_000)
            assertEquals(true, job.isActive)
            outScope.cancel()
            assertEquals(true, job.isCancelled)
            job.join()
        }

        val count = AtomicInteger(0)
        scope.launch {
            delay(2_000)
            count.incrementAndGet()
        }.let { job ->
            delay(1_000)
            assertEquals(false, job.isActive)
            job.join()
            assertEquals(0, count.get())
        }
    }
}