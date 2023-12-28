package com.sd.demo.coroutine

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutine.databinding.ActivityMainBinding

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

        _binding.btnSampleContinuation.setOnClickListener {
            startActivity(Intent(this, SampleContinuation::class.java))
        }

        _binding.btnSampleMutableFlowStore.setOnClickListener {
            startActivity(Intent(this, SampleMutableFlowStore::class.java))
        }
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("coroutine-demo", block())
}