package com.sd.demo.coroutines

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.coroutines.databinding.SampleContinuationsBinding
import com.sd.lib.coroutines.FContinuations
import com.sd.lib.coroutines.FScope
import kotlinx.coroutines.Dispatchers
import java.util.UUID

class SampleContinuations : AppCompatActivity() {
    private val _binding by lazy { SampleContinuationsBinding.inflate(layoutInflater) }

    private val _scope = FScope(lifecycleScope)

    private val _continuations = object : FContinuations<String>() {
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
            _continuations.resumeAll("hello")
        }
        _binding.btnCancelContinuations.setOnClickListener {
            logMsg { "click cancel continuations" }
            _continuations.cancel()
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
            _continuations.await()
        } catch (e: Throwable) {
            logMsg { "$tag error:$e $uuid" }
            throw e
        }

        logMsg { "$tag finish ($result) $uuid" }
    }
}