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

        _binding.btnScope.setOnClickListener {
            startActivity(Intent(this, SampleScopeActivity::class.java))
        }
        _binding.btnMutator.setOnClickListener {
            startActivity(Intent(this, SampleMutatorActivity::class.java))
        }
        _binding.btnContinuation.setOnClickListener {
            startActivity(Intent(this, SampleContinuationActivity::class.java))
        }
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("coroutine-demo", block())
}