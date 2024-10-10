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
      val syncable = FSyncable {
         delay(1_000)
         1
      }
      assertEquals(1, syncable.sync().getOrThrow())
   }

   @Test
   fun `test sync failure`(): Unit = runTest {
      val syncable = FSyncable {
         delay(1_000)
         error("sync failure")
      }
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
         assertEquals(1, syncable.sync().getOrThrow())
      }

      delay(1_000)
      repeat(3) {
         launch {
            assertEquals(1, syncable.sync().getOrThrow())
         }
      }

      advanceUntilIdle()
      assertEquals(1, count.get())
   }

   @Test
   fun `test cancel onSync`(): Unit = runTest {
      val syncable = FSyncable {
         delay(5_000)
         currentCoroutineContext().cancel()
      }

      launch {
         val result: Any = try {
            syncable.sync()
         } catch (e: Throwable) {
            e
         }
         assertEquals(true, result is CancellationException)
      }

      delay(1_000)
      repeat(3) {
         launch {
            val result: Any = try {
               syncable.sync()
            } catch (e: Throwable) {
               e
            }
            assertEquals(true, (result as Result<*>).exceptionOrNull()!! is CancellationException)
         }
      }
   }

   @Test
   fun `test cancel first sync`(): Unit = runTest {
      val scope = TestScope()

      val syncable = FSyncable {
         delay(Long.MAX_VALUE)
      }

      scope.launch {
         val result: Any = try {
            syncable.sync()
         } catch (e: Throwable) {
            e
         }
         assertEquals(true, result is CancellationException)
      }

      delay(1_000)
      repeat(3) {
         launch {
            val result: Any = try {
               syncable.sync()
            } catch (e: Throwable) {
               e
            }
            assertEquals(true, (result as Result<*>).exceptionOrNull()!! is CancellationException)
         }
      }

      delay(1_000)
      scope.cancel()
   }
}