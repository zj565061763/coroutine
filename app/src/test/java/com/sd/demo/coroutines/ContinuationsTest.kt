package com.sd.demo.coroutines

import com.sd.lib.coroutines.FContinuations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class ContinuationsTest {
   @Test
   fun `test resumeAll`(): Unit = runTest {
      val continuations = FContinuations<Int>()
      val count = AtomicInteger(0)

      repeat(3) {
         launch {
            val result = continuations.await()
            count.updateAndGet {
               it + result
            }
         }
      }

      delay(1_000)

      // resumeAll
      continuations.resumeAll(1)
      continuations.resumeAll(2)

      advanceUntilIdle()
      assertEquals(3, count.get())
   }

   @Test
   fun `test resumeAllWithException`(): Unit = runBlocking {
      val continuations = FContinuations<Int>()

      val count = AtomicInteger(0)
      val jobs = mutableSetOf<Job>()

      repeat(5) {
         launch {
            val result = try {
               continuations.await()
            } catch (e: Throwable) {
               assertEquals("resumeAllWithException 1", e.message)
               1
            }
            count.updateAndGet { it + result }
         }.also { job ->
            jobs.add(job)
         }
      }

      assertEquals(5, jobs.size)
      delay(1_000)

      // resumeAllWithException
      continuations.resumeAllWithException(Exception("resumeAllWithException 1"))
      continuations.resumeAllWithException(Exception("resumeAllWithException 2"))

      jobs.joinAll()
      assertEquals(5, count.get())
   }

   @Test
   fun `test cancelAll`(): Unit = runBlocking {
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

      // cancelAll
      continuations.cancelAll()
      continuations.cancelAll()

      jobs.joinAll()
      jobs.forEach { assertEquals(true, it.isCancelled) }
      assertEquals(0, count.get())
   }

   @Test
   fun `test cancelAll with cause`(): Unit = runBlocking {
      val continuations = FContinuations<Int>()

      val count = AtomicInteger(0)
      val jobs = mutableSetOf<Job>()

      val repeat = 5
      repeat(repeat) {
         launch {
            val result = try {
               continuations.await()
            } catch (e: Throwable) {
               assertEquals("cancelAll with cause 1", e.message)
               1
            }
            count.updateAndGet { it + result }
         }.also { job ->
            jobs.add(job)
         }
      }

      assertEquals(repeat, jobs.size)
      delay(1_000)

      // cancelAll with cause
      continuations.cancelAll(Exception("cancelAll with cause 1"))
      continuations.cancelAll(Exception("cancelAll with cause 2"))

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
}