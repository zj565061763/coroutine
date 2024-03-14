package com.sd.demo.coroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutine.databinding.SampleMutatorBinding
import com.sd.lib.coroutine.FMutator
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class SampleMutator : AppCompatActivity() {
    private val _binding by lazy { SampleMutatorBinding.inflate(layoutInflater) }
    private val _scope = MainScope()
    private val _mutator = FMutator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnMutate1.setOnClickListener {
            _scope.launch {
                _mutator.mutate {
                    start("mutate_1")
                }
            }
        }

        _binding.btnMutate2.setOnClickListener {
            _scope.launch {
                _mutator.mutate(priority = 1) {
                    start("mutate_2")
                }
            }
        }

        _binding.btnCancel.setOnClickListener {
            _mutator.cancel()
        }
    }

    private suspend fun start(tag: String) {
        val uuid = UUID.randomUUID().toString()
        logMsg { "$tag delay before $uuid" }

        try {
            delay(10_000)
        } catch (e: Exception) {
            logMsg { "$tag delay Exception:$e $uuid" }
            throw e
        }

        logMsg { "$tag delay after $uuid" }
    }

    override fun onDestroy() {
        super.onDestroy()
        _scope.cancel()
    }
}