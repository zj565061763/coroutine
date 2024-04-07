package com.sd.demo.coroutine

import com.sd.lib.coroutine.FMutator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MutatorTest {
    @Test
    fun `test cancel other`(): Unit = runBlocking {
        val mutator = FMutator()
        launch {
            mutator.mutate { delay(Long.MAX_VALUE) }
        }.let { job ->
            delay(1_000)
            mutator.mutate { }
            assertEquals(true, job.isCancelled)
        }
    }

    @Test
    fun `test cancel high priority`(): Unit = runBlocking {
        val mutator = FMutator()
        launch {
            mutator.mutate { delay(Long.MAX_VALUE) }
        }.let { job ->
            delay(1_000)
            mutator.mutate(1) { }
            assertEquals(true, job.isCancelled)
        }
    }

    @Test
    fun `test cancel self`(): Unit = runBlocking {
        val mutator = FMutator()

        val job = launch {
            mutator.mutate(1) { delay(2_000) }
        }

        delay(1_000)
        val result = try {
            mutator.mutate { }
            "mutate"
        } catch (e: CancellationException) {
            "CancellationException"
        }
        assertEquals(result, "CancellationException")
        assertEquals(true, job.isActive)
    }

    @Test
    fun `test cancel`(): Unit = runBlocking {
        val mutator = FMutator()
        launch {
            mutator.mutate { delay(Long.MAX_VALUE) }
        }.let { job ->
            delay(1_000)
            mutator.cancel()
            assertEquals(true, job.isCancelled)
        }
    }
}