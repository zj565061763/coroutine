package com.sd.demo.coroutine

import com.sd.lib.coroutine.FMutator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MutatorTest {
    @Test
    fun `test mutate`(): Unit = runBlocking {
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
    fun `test mutate high priority`(): Unit = runBlocking {
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
    fun `test mutate cancel self`(): Unit = runBlocking {
        val mutator = FMutator()
        launch {
            mutator.mutate { delay(Long.MAX_VALUE) }
        }.let { job ->
            delay(1_000)
            val result = try {
                mutator.mutate(-1) { }
                "mutate"
            } catch (e: CancellationException) {
                "CancellationException"
            }
            assertEquals(result, "CancellationException")
            assertEquals(true, job.isActive)
            job.cancelAndJoin()
        }
    }

    @Test
    fun `test cancel`(): Unit = runBlocking {
        val mutator = FMutator()
        launch {
            mutator.mutate { delay(Long.MAX_VALUE) }
        }.let { job ->
            delay(1_000)
            repeat(1_000) {
                launch {
                    mutator.cancel()
                    assertEquals(true, job.isCancelled)
                }
            }
        }
    }
}