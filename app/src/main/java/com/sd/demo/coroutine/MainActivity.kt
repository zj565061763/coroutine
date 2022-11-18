package com.sd.demo.coroutine

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutine.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
    }

    override fun onClick(v: View) {
        when (v) {
            _binding.btnMutator -> startActivity(Intent(this, SampleMutatorActivity::class.java))
            _binding.btnScope -> startActivity(Intent(this, SampleScopeActivity::class.java))
            _binding.btnContinuation -> startActivity(Intent(this, SampleContinuationActivity::class.java))
        }
    }
}

fun logMsg(block: () -> String) {
    Log.i("FCoroutine-demo", block())
}