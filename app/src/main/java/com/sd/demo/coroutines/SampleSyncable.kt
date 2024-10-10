package com.sd.demo.coroutines

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.coroutines.databinding.SampleSyncableBinding
import com.sd.lib.coroutines.FSyncable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class SampleSyncable : AppCompatActivity() {
   private val _binding by lazy { SampleSyncableBinding.inflate(layoutInflater) }

   private val _syncable = FSyncable { loadData() }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(_binding.root)
      _binding.btnSync.setOnClickListener {
         logMsg { "click sync" }
         lifecycleScope.launch {
            syncAwait()
         }
      }
   }

   private suspend fun syncAwait() {
      val uuid = UUID.randomUUID().toString()
      logMsg { "syncAwait start $uuid" }

      val result = try {
         _syncable.sync()
      } catch (e: Throwable) {
         logMsg { "syncAwait error:$e $uuid" }
         throw e
      }

      result.onSuccess {
         logMsg { "syncAwait onSuccess $it $uuid" }
      }
      result.onFailure {
         logMsg { "syncAwait onFailure $it $uuid" }
      }
   }

   private suspend fun loadData(): Int {
      logMsg { "loadData start" }
      try {
         delay(5_000)
      } catch (e: Throwable) {
         logMsg { "loadData error:$e" }
         throw e
      }
      logMsg { "loadData finish" }
      return 1
   }
}