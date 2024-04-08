package com.sd.demo.coroutines

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.coroutines.databinding.SampleContinuationBinding
import com.sd.lib.coroutines.FContinuations
import com.sd.lib.coroutines.FScope
import kotlinx.coroutines.Dispatchers
import java.util.UUID

class SampleContinuation : AppCompatActivity() {
    private val _binding by lazy { SampleContinuationBinding.inflate(layoutInflater) }

    private val _scope = FScope(lifecycleScope)

    private val _continuation = object : FContinuations<String>() {
        override fun onFirstAwait() {
            logMsg { "onFirstAwait ${Thread.currentThread().name}" }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnLaunch.setOnClickListener {
            _scope.launch(Dispatchers.IO) { start("launch") }
        }
        _binding.btnResume.setOnClickListener {
            logMsg { "click resume" }
            _continuation.resume("hello")
        }
        _binding.btnCancelContinuation.setOnClickListener {
            logMsg { "click cancel continuation" }
            _continuation.cancel()
        }
        _binding.btnCancelLaunch.setOnClickListener {
            logMsg { "click cancel launch" }
            _scope.cancel()
        }
    }

    private suspend fun start(tag: String) {
        val uuid = UUID.randomUUID().toString()
        logMsg { "$tag start $uuid" }

        val result = try {
            _continuation.await()
        } catch (e: Exception) {
            logMsg { "$tag Exception:$e $uuid" }
            throw e
        }

        logMsg { "$tag finish ($result) $uuid" }
    }
}