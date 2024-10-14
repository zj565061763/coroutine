package com.sd.demo.coroutines

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
   fun `test sync success`(): Unit = runTest {
      val syncable = FSyncable {
         delay(1_000)
         1
      }
      val result = syncable.syncWithResult()
      assertEquals(1, result.getOrThrow())
   }

   @Test
   fun `test sync failure`(): Unit = runTest {
      val syncable = FSyncable {
         delay(1_000)
         error("sync error")
      }
      val result = syncable.syncWithResult()
      assertEquals("sync error", result.exceptionOrNull()!!.message)
   }

   @Test
   fun `test sync success when busy`(): Unit = runTest {
      val count = AtomicInteger(0)

      val syncable = FSyncable {
         // 5秒后返回结果
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

      advanceUntilIdle()
      assertEquals(5, count.get())
   }

   @Test
   fun `test cancel in onSync`(): Unit = runTest {
      val syncable = FSyncable {
         // 5秒后取消执行
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
   fun `test cancel first sync`(): Unit = runTest {
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
   fun `test cancel other sync`(): Unit = runTest {
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

      job1.cancel()
      advanceUntilIdle()
      assertEquals(true, job1.isCancelled)
      assertEquals(true, job2.isCancelled)
   }
}