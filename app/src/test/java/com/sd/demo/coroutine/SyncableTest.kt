package com.sd.demo.coroutine

import com.sd.lib.coroutine.FSyncable
import kotlinx.coroutines.CancellationException
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

        syncable.sync()
        syncable.sync()

        assertEquals(1, syncable.syncAwait().getOrThrow())
        assertEquals(2, syncable.syncAwait().getOrThrow())
    }

    @Test
    fun `test sync failure`(): Unit = runBlocking {
        val syncable = FSyncable(scope = this) {
            error("failure")
        }

        syncable.syncAwait().let { result ->
            assertEquals("failure", result.exceptionOrNull()!!.message)
        }
    }

    @Test
    fun `test cancel onSync`(): Unit = runBlocking {
        val syncable = FSyncable(scope = this) {
            currentCoroutineContext().cancel()
        }

        try {
            syncable.syncAwait()
        } catch (e: Throwable) {
            assertEquals(true, e is CancellationException)
        }
    }
}