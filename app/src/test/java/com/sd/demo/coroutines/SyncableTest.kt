package com.sd.demo.coroutines

import com.sd.lib.coroutines.FSyncable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class SyncableTest {
   @get:Rule
   val mainDispatcherRule = MainDispatcherRule()

   @Test
   fun `test sync success`(): Unit = runTest {
      val count = AtomicInteger(0)

      val syncable = FSyncable {
         delay(10)
         count.incrementAndGet()
      }

      assertEquals(1, syncable.sync().getOrThrow())
      assertEquals(2, syncable.sync().getOrThrow())
      assertEquals(3, syncable.sync().getOrThrow())
   }

   @Test
   fun `test sync failure`(): Unit = runTest {
      val syncable = FSyncable {
         error("failure")
      }

      syncable.sync().let { result ->
         assertEquals("failure", result.exceptionOrNull()!!.message)
      }
   }

   @Test
   fun `test cancel onSync`(): Unit = runTest {
      val syncable = FSyncable {
         delay(10)
         currentCoroutineContext().cancel()
      }

      launch {
         syncable.sync().let { result ->
            assertEquals(true, result.exceptionOrNull()!! is CancellationException)
         }
      }
   }

   @Test
   fun `test cancel scope`(): Unit = runTest {
      val outScope = TestScope()

      val syncable = FSyncable {
         delay(10)
         outScope.cancel()
      }

      outScope.launch {
         try {
            syncable.sync()
         } catch (e: Throwable) {
            Result.failure(e)
         }.let { result ->
            assertEquals(true, result.exceptionOrNull()!! is CancellationException)
         }
      }
   }
}