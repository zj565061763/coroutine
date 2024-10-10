package com.sd.demo.coroutines

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.coroutines.databinding.SampleMutatorBinding
import com.sd.lib.coroutines.FMutator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class SampleMutator : AppCompatActivity() {
   private val _binding by lazy { SampleMutatorBinding.inflate(layoutInflater) }
   private val _mutator = FMutator()

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(_binding.root)
      _binding.btnMutate1.setOnClickListener {
         lifecycleScope.launch {
            logMsg { "click mutate_1" }
            _mutator.mutate {
               start("mutate_1")
            }
         }
      }
      _binding.btnMutate2.setOnClickListener {
         lifecycleScope.launch {
            logMsg { "click mutate_2" }
            _mutator.mutate(priority = 1) {
               start("mutate_2")
            }
         }
      }
      _binding.btnCancel.setOnClickListener {
         logMsg { "click cancel" }
         _mutator.cancel()
      }
      _binding.btnCancelAndJoin.setOnClickListener {
         logMsg { "click cancelAndJoin" }
         lifecycleScope.launch {
            _mutator.cancelAndJoin()
         }
      }
   }

   private suspend fun start(tag: String) {
      val uuid = UUID.randomUUID().toString()
      logMsg { "$tag start $uuid" }

      try {
         delay(Long.MAX_VALUE)
      } catch (e: Throwable) {
         logMsg { "$tag error:$e $uuid" }
         throw e
      }

      logMsg { "$tag finish $uuid" }
   }
}