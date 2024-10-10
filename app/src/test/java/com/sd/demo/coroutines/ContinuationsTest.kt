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
   fun `test onFirstAwait resumeAll`(): Unit = runBlocking {
      val continuations = object : FContinuations<Int>() {
         override fun onFirstAwait() {
            // resumeAll
            resumeAll(1)
            resumeAll(2)
         }
      }
      assertEquals(1, continuations.await())
   }

   @Test
   fun `test onFirstAwait resumeAllWithException`(): Unit = runBlocking {
      val continuations = object : FContinuations<Int>() {
         override fun onFirstAwait() {
            // resumeAllWithException
            resumeAllWithException(Exception("resumeAllWithException 1"))
            resumeAllWithException(Exception("resumeAllWithException 2"))
         }
      }

      val result = try {
         continuations.await()
      } catch (e: Throwable) {
         assertEquals("resumeAllWithException 1", e.message)
         1
      }

      assertEquals(1, result)
   }

   @Test
   fun `test onFirstAwait cancelAll`(): Unit = runBlocking {
      val continuations = object : FContinuations<Int>() {
         override fun onFirstAwait() {
            // cancelAll
            cancelAll()
            cancelAll()
         }
      }

      val result = try {
         continuations.await()
      } catch (e: CancellationException) {
         1
      }

      assertEquals(1, result)
   }

   @Test
   fun `test onFirstAwait cancelAll with cause`(): Unit = runBlocking {
      val continuations = object : FContinuations<Int>() {
         override fun onFirstAwait() {
            // cancelAll with cause
            cancelAll(Exception("cancelAll with cause 1"))
            cancelAll(Exception("cancelAll with cause 2"))
         }
      }

      val result = try {
         continuations.await()
      } catch (e: Throwable) {
         assertEquals("cancelAll with cause 1", e.message)
         1
      }

      assertEquals(1, result)
   }
}