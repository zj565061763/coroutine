package com.sd.demo.coroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutine.databinding.SampleScopeBinding
import com.sd.lib.coroutine.FScope
import kotlinx.coroutines.delay
import java.util.UUID

class SampleScope : AppCompatActivity() {
    private val _binding by lazy { SampleScopeBinding.inflate(layoutInflater) }

    private val _scope = FScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnLaunch.setOnClickListener {
            _scope.launch { start("launch") }
            logMsg { "launch size:${_scope.size()}" }
        }
        _binding.btnCancel.setOnClickListener {
            _scope.cancel()
            logMsg { "cancel size:${_scope.size()}" }
        }
    }

    private suspend fun start(tag: String) {
        val uuid = UUID.randomUUID().toString()
        logMsg { "$tag start $uuid" }

        try {
            delay(10_000)
        } catch (e: Exception) {
            logMsg { "$tag Exception:$e $uuid" }
            throw e
        }

        logMsg { "$tag finish $uuid" }
    }

    override fun onDestroy() {
        super.onDestroy()
        _scope.cancel()
    }
}