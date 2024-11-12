package com.sd.demo.coroutines

import app.cash.turbine.test
import com.sd.lib.coroutines.FSyncable
import com.sd.lib.coroutines.awaitIdle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
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
   fun `test sync when success`() = runTest {
      val syncable = FSyncable { 1 }
      val result = syncable.syncWithResult()
      assertEquals(1, result.getOrThrow())
   }

   @Test
   fun `test sync when error in block`() = runTest {
      val syncable = FSyncable { error("sync error") }
      val result = syncable.syncWithResult()
      assertEquals("sync error", result.exceptionOrNull()!!.message)
   }

   @Test
   fun `test sync cancel when throw CancellationException in block`() = runTest {
      val syncable = FSyncable { throw CancellationException() }
      launch {
         syncable.syncWithResult()
      }.also { job ->
         runCurrent()
         assertEquals(true, job.isCancelled)
      }
   }

   @Test
   fun `test sync cancel when cancel in block`() = runTest {
      val syncable = FSyncable { currentCoroutineContext().cancel() }
      launch {
         syncable.syncWithResult()
      }.also { job ->
         runCurrent()
         assertEquals(true, job.isCancelled)
      }
   }

   @Test
   fun `test syncing flow when success`() = runTest {
      val syncable = FSyncable { 1 }
      syncable.syncingFlow.test {
         syncable.syncWithResult()
         assertEquals(false, awaitItem())
         assertEquals(true, awaitItem())
         assertEquals(false, awaitItem())
      }
   }

   @Test
   fun `test syncing flow when error in block`() = runTest {
      val syncable = FSyncable { error("error") }
      syncable.syncingFlow.test {
         syncable.syncWithResult()
         assertEquals(false, awaitItem())
         assertEquals(true, awaitItem())
         assertEquals(false, awaitItem())
      }
   }

   @Test
   fun `test syncing flow when when throw CancellationException in block`() = runTest {
      val syncable = FSyncable { throw CancellationException() }
      syncable.syncingFlow.test {
         launch {
            syncable.syncWithResult()
         }.also { job ->
            runCurrent()
            assertEquals(true, job.isCancelled)
         }
         assertEquals(false, awaitItem())
         assertEquals(true, awaitItem())
         assertEquals(false, awaitItem())
      }
   }

   @Test
   fun `test syncing flow when cancel in block`() = runTest {
      val syncable = FSyncable { currentCoroutineContext().cancel() }
      syncable.syncingFlow.test {
         launch {
            syncable.syncWithResult()
         }.also { job ->
            runCurrent()
            assertEquals(true, job.isCancelled)
         }
         assertEquals(false, awaitItem())
         assertEquals(true, awaitItem())
         assertEquals(false, awaitItem())
      }
   }

   @Test
   fun `test syncing flow when cancel sync`() = runTest {
      val syncable = FSyncable { delay(Long.MAX_VALUE) }
      syncable.syncingFlow.test {
         launch {
            syncable.syncWithResult()
         }.also { job ->
            runCurrent()
            job.cancelAndJoin()
         }
         assertEquals(false, awaitItem())
         assertEquals(true, awaitItem())
         assertEquals(false, awaitItem())
      }
   }

   @Test
   fun `test awaitIdle`() = runTest {
      val count = AtomicInteger(0)
      val syncable = FSyncable {
         delay(5_000)
      }

      launch {
         syncable.syncWithResult()
      }.also {
         runCurrent()
      }

      launch {
         syncable.awaitIdle()
         count.incrementAndGet()
      }.also {
         runCurrent()
         assertEquals(0, count.get())
      }

      advanceUntilIdle()
      assertEquals(1, count.get())
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

   @Test
   fun `test reSync`() = runTest {
      val array = arrayOf<FSyncable<*>?>(null)
      FSyncable {
         delay(1_000)
         runCatching {
            array[0]!!.syncWithResult()
         }.also { result ->
            assertEquals("Can not call sync in the onSync block.", result.exceptionOrNull()!!.message)
         }
         1
      }.also {
         array[0] = it
         assertEquals(1, it.syncWithResult().getOrThrow())
      }
   }
}