package com.sd.demo.coroutines

import com.sd.lib.coroutines.fAwait
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

class AwaitTest {
   @Test
   fun `test resume`(): Unit = runBlocking {
      val count = AtomicInteger(0)
      val result = fAwait(
         onError = { count.incrementAndGet() },
      ) { cont ->
         cont.resume(1)

         // onError
         cont.resume(2)
         // onError
         cont.resume(3)
      }

      assertEquals(1, result)
      assertEquals(2, count.get())
   }
}