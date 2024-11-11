package com.sd.demo.coroutines

import app.cash.turbine.test
import com.sd.lib.coroutines.FSyncable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class SyncableTest {
   @get:Rule
   val mainDispatcherRule = MainDispatcherRule()

   @Test
   fun `test sync success`() = runTest {
      val syncable = FSyncable {
         delay(1_000)
         1
      }
      val result = syncable.syncWithResult()
      assertEquals(1, result.getOrThrow())
   }

   @Test
   fun `test sync failure`() = runTest {
      val syncable = FSyncable {
         delay(1_000)
         error("sync error")
      }
      val result = syncable.syncWithResult()
      assertEquals("sync error", result.exceptionOrNull()!!.message)
   }

   @Test
   fun `test syncing flow`() = runTest {
      val syncable = FSyncable { 1 }
      syncable.syncingFlow.test {
         assertEquals(false, awaitItem())
         syncable.syncWithResult()
         assertEquals(true, awaitItem())
         assertEquals(false, awaitItem())
      }
   }

   @Test
   fun `test sync success when busy`() = runTest {
      val count = AtomicInteger(0)

      val syncable = FSyncable {
         delay(5_000)
         count.incrementAndGet()
      }

      // 启动第1个协程，执行真正的逻辑，并等待结果
      val job1 = launch {
         val result = syncable.syncWithResult()
         assertEquals(1, result.getOrThrow())
         count.incrementAndGet()
      }.also {
         runCurrent()
         assertEquals(true, it.isActive)
         assertEquals(0, count.get())
      }

      // 启动3个协程，等待结果
      repeat(3) {
         launch {
            val result = syncable.syncWithResult()
            assertEquals(1, result.getOrThrow())
            count.incrementAndGet()
         }
      }

      runCurrent()
      assertEquals(true, job1.isActive)
      assertEquals(0, count.get())

      advanceUntilIdle()
      assertEquals(5, count.get())
   }

   @Test
   fun `test cancel in onSync`() = runTest {
      val syncable = FSyncable {
         delay(5_000)
         throw CancellationException()
      }

      val jobs = mutableSetOf<Job>()
      repeat(3) {
         launch {
            syncable.syncWithResult()
         }.also {
            jobs.add(it)
         }
      }

      runCurrent()
      assertEquals(3, jobs.size)
      jobs.forEach {
         assertEquals(true, it.isActive)
      }

      advanceUntilIdle()
      assertEquals(3, jobs.size)
      jobs.forEach {
         assertEquals(true, it.isCancelled)
      }
   }

   @Test
   fun `test cancel first sync`() = runTest {
      val syncable = FSyncable {
         delay(Long.MAX_VALUE)
      }

      val job1 = launch {
         syncable.syncWithResult()
      }.also {
         runCurrent()
         assertEquals(true, it.isActive)
      }

      val job2 = launch {
         syncable.syncWithResult()
      }.also {
         runCurrent()
         assertEquals(true, it.isActive)
      }

      job1.cancel()
      advanceUntilIdle()
      assertEquals(true, job1.isCancelled)
      assertEquals(true, job2.isCancelled)
   }

   @Test
   fun `test cancel other sync`() = runTest {
      val syncable = FSyncable {
         delay(Long.MAX_VALUE)
      }

      val job1 = launch {
         syncable.syncWithResult()
      }.also {
         runCurrent()
         assertEquals(true, it.isActive)
      }

      val job2 = launch {
         syncable.syncWithResult()
      }.also {
         runCurrent()
         assertEquals(true, it.isActive)
      }

      job2.cancelAndJoin()
      assertEquals(true, job1.isActive)
      assertEquals(true, job2.isCancelled)

      job1.cancelAndJoin()
      assertEquals(true, job1.isCancelled)
      assertEquals(true, job2.isCancelled)
   }
}