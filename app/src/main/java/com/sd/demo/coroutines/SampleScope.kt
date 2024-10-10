package com.sd.demo.coroutines

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.coroutines.databinding.SampleScopeBinding
import com.sd.lib.coroutines.FScope
import kotlinx.coroutines.delay
import java.util.UUID

class SampleScope : AppCompatActivity() {
   private val _binding by lazy { SampleScopeBinding.inflate(layoutInflater) }

   private val _scope = FScope(lifecycleScope)

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(_binding.root)
      _binding.btnLaunch.setOnClickListener {
         _scope.launch { start("launch") }
      }
      _binding.btnCancel.setOnClickListener {
         _scope.cancel()
      }
   }

   private suspend fun start(tag: String) {
      val uuid = UUID.randomUUID().toString()
      logMsg { "$tag start $uuid" }

      try {
         delay(10_000)
      } catch (e: Throwable) {
         logMsg { "$tag error:$e $uuid" }
         throw e
      }

      logMsg { "$tag finish $uuid" }
   }
}