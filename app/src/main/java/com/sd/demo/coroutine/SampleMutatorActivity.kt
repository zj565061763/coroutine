package com.sd.demo.coroutine

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutine.databinding.ActivityMutatorBinding
import com.sd.lib.coroutine.FMutator
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class SampleMutatorActivity : AppCompatActivity(), View.OnClickListener {
    private val _binding by lazy { ActivityMutatorBinding.inflate(layoutInflater) }

    private val _scope = MainScope()
    private val _mutator = FMutator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
    }

    override fun onClick(v: View) {
        when (v) {
            _binding.btnMutate1 -> {
                _scope.launch {
                    _mutator.mutate {
                        start("mutate_1")
                    }
                }
            }
            _binding.btnMutate2 -> {
                _scope.launch {
                    _mutator.mutate(priority = 1) {
                        start("mutate_2")
                    }
                }
            }
            _binding.btnCancelMutate -> {
                _mutator.cancel()
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