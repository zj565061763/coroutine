package com.sd.demo.coroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutine.databinding.SampleContinuationBinding
import com.sd.lib.coroutine.FContinuation
import com.sd.lib.coroutine.FScope
import java.util.UUID

class SampleContinuation : AppCompatActivity() {
    private val _binding by lazy { SampleContinuationBinding.inflate(layoutInflater) }
    private val _scope = FScope()
    private val _continuation = FContinuation<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnAwait.setOnClickListener {
            _scope.launch {
                start("launch")
            }
        }
        _binding.btnResume.setOnClickListener {
            _continuation.resume("hello")
            logMsg { "size:${_continuation.size()}" }
        }
        _binding.btnCancel.setOnClickListener {
            _continuation.cancel()
            logMsg { "size:${_continuation.size()}" }
        }
    }

    private suspend fun start(tag: String) {
        val uuid = UUID.randomUUID().toString()
        logMsg { "$tag before $uuid" }

        try {
            val result = _continuation.await()
            logMsg { "$tag result:$result $uuid" }
        } catch (e: Exception) {
            logMsg { "$tag Exception:$e $uuid" }
            throw e
        }

        logMsg { "$tag after $uuid" }
    }

    override fun onDestroy() {
        super.onDestroy()
        _scope.cancel()
    }
}