package com.sd.demo.coroutine

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutine.databinding.ActivityScopeBinding
import com.sd.lib.coroutine.FScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import java.util.*

class SampleScopeActivity : AppCompatActivity(), View.OnClickListener {
    private val _binding by lazy { ActivityScopeBinding.inflate(layoutInflater) }

    private val _scope = FScope(MainScope())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
    }

    override fun onClick(v: View) {
        when (v) {
            _binding.btnLaunch -> {
                _scope.launch {
                    start("launch")
                }
            }
            _binding.btnCancel -> {
                _scope.cancel()
            }
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