package com.sd.demo.coroutines

import com.sd.lib.coroutines.FSyncable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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
   fun `test sync when busy`(): Unit = runTest {
      val count = AtomicInteger(0)

      val syncable = FSyncable {
         // 5秒后返回结果
         delay(5_000)
         count.incrementAndGet()
      }

      // 第1个协程，执行真正的逻辑，并等待结果
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
         }.also { job ->
            runCurrent()
            assertEquals(true, job.isActive)
            assertEquals(true, job1.isActive)
         }
      }

      advanceUntilIdle()
      assertEquals(5, count.get())
   }

   @Test
   fun `test cancel onSync`(): Unit = runTest {
      val count = AtomicInteger()

      val syncable = FSyncable {
         count.getAndIncrement()
         delay(5_000)
         currentCoroutineContext().cancel()
      }

      launch {
         val result: Any = try {
            syncable.syncWithResult()
         } catch (e: Throwable) {
            e
         }
         assertEquals(true, result is CancellationException)
         count.getAndIncrement()
      }.also {
         // 等待第1个协程启动
         runCurrent()
      }

      launch {
         val result: Any = try {
            syncable.syncWithResult()
         } catch (e: Throwable) {
            e
         }
         assertEquals(true, result is CancellationException)
         count.getAndIncrement()
      }.also {
         // 等待第2个协程启动
         runCurrent()
      }

      advanceUntilIdle()
      assertEquals(3, count.get())
   }

   @Test
   fun `test cancel first sync`(): Unit = runTest {
      val count = AtomicInteger()

      val syncable = FSyncable {
         count.getAndIncrement()
         delay(Long.MAX_VALUE)
      }

      val job = launch {
         val result: Any = try {
            syncable.syncWithResult()
         } catch (e: Throwable) {
            e
         }
         assertEquals(true, result is CancellationException)
         count.getAndIncrement()
      }.also {
         // 等待第1个协程启动
         runCurrent()
      }

      launch {
         val result: Any = try {
            syncable.syncWithResult()
         } catch (e: Throwable) {
            e
         }
         assertEquals(true, result is CancellationException)
         count.getAndIncrement()
      }.also {
         // 等待第2个协程启动
         runCurrent()
      }

      // 取消第一个协程
      job.cancel()

      advanceUntilIdle()
      assertEquals(3, count.get())
   }
}