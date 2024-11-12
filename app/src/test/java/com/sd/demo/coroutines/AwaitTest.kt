package com.sd.demo.coroutines

import com.sd.lib.coroutines.fAwait
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.coroutines.resume

class AwaitTest {
   @Test
   fun `test resume`() = runTest {
      fAwait { cont ->
         cont.resume(1)
         cont.resume(2)
         cont.resume(3)
      }.also { result ->
         assertEquals(1, result)
      }
   }
}