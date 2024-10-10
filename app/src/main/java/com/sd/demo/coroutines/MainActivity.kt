package com.sd.demo.coroutines

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutines.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
   private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(_binding.root)
      _binding.btnSampleScope.setOnClickListener {
         startActivity(Intent(this, SampleScope::class.java))
      }
      _binding.btnSampleMutator.setOnClickListener {
         startActivity(Intent(this, SampleMutator::class.java))
      }
      _binding.btnSampleContinuations.setOnClickListener {
         startActivity(Intent(this, SampleContinuations::class.java))
      }
      _binding.btnSampleSyncable.setOnClickListener {
         startActivity(Intent(this, SampleSyncable::class.java))
      }
   }
}

inline fun logMsg(block: () -> String) {
   Log.i("coroutines-demo", block())
}