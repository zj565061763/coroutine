package com.sd.demo.coroutines

import com.sd.lib.coroutines.FSyncable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class SyncableTest {

   @Test
   fun `test sync success`(): Unit = runBlocking {
      val count = AtomicInteger(0)

      val syncable = FSyncable(scope = this) {
         delay(1_000)
         count.incrementAndGet()
      }

      assertEquals(1, syncable.sync().getOrThrow())
      assertEquals(2, syncable.sync().getOrThrow())
      assertEquals(3, syncable.sync().getOrThrow())
   }

   @Test
   fun `test sync failure`(): Unit = runBlocking {
      val syncable = FSyncable(scope = this) {
         error("failure")
      }

      syncable.sync().let { result ->
         assertEquals("failure", result.exceptionOrNull()!!.message)
      }
   }

   @Test
   fun `test cancel onSync`(): Unit = runBlocking {
      val syncable = FSyncable(scope = this) {
         currentCoroutineContext().cancel()
      }

      try {
         syncable.sync()
      } catch (e: Throwable) {
         Result.failure(e)
      }.let { result ->
         assertEquals(true, result.exceptionOrNull()!! is CancellationException)
      }

      try {
         syncable.sync()
      } catch (e: Throwable) {
         Result.failure(e)
      }.let { result ->
         assertEquals(true, result.exceptionOrNull()!! is CancellationException)
      }
   }

   @Test
   fun `test cancel scope`(): Unit = runBlocking {
      val outScope = CoroutineScope(SupervisorJob())

      val syncable = FSyncable(scope = outScope) {
         outScope.cancel()
      }

      try {
         syncable.sync()
      } catch (e: Throwable) {
         Result.failure(e)
      }.let { result ->
         assertEquals(true, result.exceptionOrNull()!! is CancellationException)
      }

      try {
         syncable.sync()
      } catch (e: Throwable) {
         Result.failure(e)
      }.let { result ->
         assertEquals(true, result.exceptionOrNull()!! is CancellationException)
      }
   }
}