package com.sd.demo.coroutines

import com.sd.lib.coroutines.FScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class ScopeTest {
   @Test
   fun `test launch`(): Unit = runTest {
      val scope = FScope(this)
      testLaunchSuccess(scope)
   }

   @Test
   fun `test cancel scope`(): Unit = runTest {
      val scope = FScope(this)
      testCancelScope(scope) { scope.cancel() }
      testLaunchSuccess(scope)
   }

   @Test
   fun `test cancel out scope`(): Unit = runTest {
      val outScope = TestScope(testScheduler)
      val scope = FScope(outScope)
      testCancelScope(scope) { outScope.cancel() }

      val count = AtomicInteger(0)
      repeat(3) {
         scope.launch {
            count.incrementAndGet()
         }.let { job ->
            assertEquals(true, job.isCancelled)
         }
      }

      advanceUntilIdle()
      assertEquals(0, count.get())
   }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun TestScope.testLaunchSuccess(scope: FScope) {
   val count = AtomicInteger(0)

   repeat(3) {
      scope.launch {
         count.incrementAndGet()
      }
   }

   advanceUntilIdle()
   assertEquals(3, count.get())
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun TestScope.testCancelScope(
   scope: FScope,
   cancelScope: () -> Unit,
) {
   val jobs = mutableSetOf<Job>()

   repeat(3) {
      scope.launch {
         delay(Long.MAX_VALUE)
      }.let { job ->
         assertEquals(true, job.isActive)
         jobs.add(job)
      }
   }

   assertEquals(3, jobs.size)
   delay(1_000)

   // cancel scope
   cancelScope()

   jobs.forEach { assertEquals(true, it.isCancelled) }
   advanceUntilIdle()
}