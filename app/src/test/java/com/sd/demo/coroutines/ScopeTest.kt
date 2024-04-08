package com.sd.demo.coroutines

import com.sd.lib.coroutines.FScope
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
    fun `test launch`(): Unit = runBlocking {
        val scope = FScope(this)
        testLaunchSuccess(scope)
    }

    @Test
    fun `test cancel scope`(): Unit = runBlocking {
        val scope = FScope(this)
        testCancelScope(scope) { scope.cancel() }
        testLaunchSuccess(scope)
    }

    @Test
    fun `test cancel out scope`(): Unit = runBlocking {
        val outScope = CoroutineScope(SupervisorJob())
        val scope = FScope(outScope)

        testCancelScope(scope) { outScope.cancel() }
        testLaunchCanceledScope(scope)
    }
}

private suspend fun testLaunchSuccess(scope: FScope) {
    val count = AtomicInteger(0)
    val jobs = mutableSetOf<Job>()

    repeat(5) {
        scope.launch {
            count.incrementAndGet()
        }.let { job ->
            assertEquals(true, job.isActive)
            jobs.add(job)
        }
    }

    assertEquals(5, jobs.size)
    jobs.joinAll()
    assertEquals(5, count.get())
}

private suspend fun testCancelScope(
    scope: FScope,
    cancelScope: () -> Unit,
) {
    val jobs = mutableSetOf<Job>()

    repeat(5) {
        scope.launch {
            delay(Long.MAX_VALUE)
        }.let { job ->
            assertEquals(true, job.isActive)
            jobs.add(job)
        }
    }

    assertEquals(5, jobs.size)
    delay(1_000)

    // cancel scope
    cancelScope()

    jobs.forEach { assertEquals(true, it.isCancelled) }
    jobs.joinAll()
}

private suspend fun testLaunchCanceledScope(scope: FScope) {
    val count = AtomicInteger(0)
    val jobs = mutableSetOf<Job>()

    repeat(5) {
        scope.launch {
            count.incrementAndGet()
        }.let { job ->
            assertEquals(false, job.isActive)
            jobs.add(job)
        }
    }

    assertEquals(5, jobs.size)
    jobs.joinAll()
    assertEquals(0, count.get())
}