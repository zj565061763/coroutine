package com.sd.demo.coroutines

import com.sd.lib.coroutines.FSyncable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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
      val syncable = FSyncable { 1 }
      assertEquals(1, syncable.sync().getOrThrow())
   }

   @Test
   fun `test sync failure`(): Unit = runTest {
      val syncable = FSyncable { error("sync failure") }
      assertEquals("sync failure", syncable.sync().exceptionOrNull()!!.message)
   }

   @Test
   fun `test sync when busy`(): Unit = runTest {
      val count = AtomicInteger(0)

      val syncable = FSyncable {
         delay(5_000)
         count.incrementAndGet()
      }

      launch {
         syncable.sync()
      }

      delay(1_000)
      repeat(3) {
         launch {
            syncable.sync()
         }
      }

      advanceUntilIdle()
      assertEquals(1, count.get())
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