package com.sd.demo.coroutines

import com.sd.lib.coroutines.FMutator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MutatorTest {
   @Test
   fun `test mutate`() = runTest {
      val mutator = FMutator()
      launch {
         mutator.mutate { delay(Long.MAX_VALUE) }
      }.let { job ->
         runCurrent()
         mutator.mutate { }
         assertEquals(true, job.isCancelled)
         assertEquals(true, job.isCompleted)
      }
   }

   @Test
   fun `test mutate with higher priority`() = runTest {
      val mutator = FMutator()
      launch {
         mutator.mutate { delay(Long.MAX_VALUE) }
      }.let { job ->
         runCurrent()
         mutator.mutate(1) { }
         assertEquals(true, job.isCancelled)
         assertEquals(true, job.isCompleted)
      }
   }

   @Test
   fun `test mutate cancel self`() = runTest {
      val mutator = FMutator()
      launch {
         mutator.mutate { delay(Long.MAX_VALUE) }
      }.let { job ->
         runCurrent()
         val result: Any = try {
            mutator.mutate(-1) { }
         } catch (e: Throwable) {
            e
         }
         assertEquals(true, result is CancellationException)
         assertEquals(true, job.isActive)
         job.cancelAndJoin()
      }
   }

   @Test
   fun `test cancelAndJoin`() = runTest {
      val mutator = FMutator()
      launch {
         mutator.mutate { delay(Long.MAX_VALUE) }
      }.let { job ->
         runCurrent()
         mutator.cancelAndJoin()
         assertEquals(true, job.isCancelled)
         assertEquals(true, job.isCompleted)
      }
   }
}