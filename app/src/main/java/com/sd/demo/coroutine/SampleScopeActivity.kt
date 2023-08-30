package com.sd.demo.coroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutine.databinding.ActivitySampleScopeBinding
import com.sd.lib.coroutine.FScope
import kotlinx.coroutines.delay
import java.util.UUID

class SampleScopeActivity : AppCompatActivity() {
    private val _binding by lazy { ActivitySampleScopeBinding.inflate(layoutInflater) }
    private val _scope = FScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnLaunch.setOnClickListener {
            _scope.launch {
                start("launch")
            }
        }

        _binding.btnCancel.setOnClickListener {
            _scope.cancel()
        }
    }

    private suspend fun start(tag: String) {
        val uuid = UUID.randomUUID().toString()
        logMsg { "$tag delay before $uuid" }

        try {
            delay(5000)
        } catch (e: Exception) {
            logMsg { "$tag delay Exception:$e $uuid" }
            e.printStackTrace()
            throw e
        }

        logMsg { "$tag delay after $uuid" }
    }

    override fun onDestroy() {
        super.onDestroy()
        _scope.cancel()
    }
}