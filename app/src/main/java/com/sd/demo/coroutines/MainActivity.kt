package com.sd.demo.coroutines

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.coroutines.databinding.ActivityMainBinding
import com.sd.lib.coroutines.FLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
   private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(_binding.root)

      val loader = FLoader()

      _binding.btn.setOnClickListener {
         lifecycleScope.launch {
            loader.load { logMsg { "click load" } }
         }
      }

      val flow = (1..10)
         .asFlow()
//         .onEach { logMsg { "onEach" } }
//         .buffer()

      lifecycleScope.launch {
         flow.collect {
            loader.load {
               logMsg { it.toString() }
               delay(1_000)
            }
         }
      }
   }
}


inline fun logMsg(block: () -> String) {
   Log.i("coroutines-demo", block())
}