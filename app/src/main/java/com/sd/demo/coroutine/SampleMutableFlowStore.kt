package com.sd.demo.coroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutine.databinding.SampleMutableFlowStoreBinding
import com.sd.lib.coroutine.FMutableFlowStore
import com.sd.lib.coroutine.FScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class SampleMutableFlowStore : AppCompatActivity() {
    private val _binding by lazy { SampleMutableFlowStoreBinding.inflate(layoutInflater) }

    private val _scope = FScope()
    private val _store = FMutableFlowStore<MutableSharedFlow<Int>>()

    private var _count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnCollect.setOnClickListener {
            collectFlow()
        }
        _binding.btnCancelCollect.setOnClickListener {
            logMsg { "click cancel" }
            _scope.cancel()
        }
        _binding.btnEmit.setOnClickListener {
            _store.get("")?.tryEmit(++_count)
        }
        _binding.btnLog.setOnClickListener {
            logMsg { "store size:${_store.size()} collect size:${_scope.size()}" }
        }
    }

    private fun collectFlow() {
        val flow = _store.getOrPut("") { MutableStateFlow(0) }
        _scope.launch {
            flow.collect {
                logMsg { "collect $it" }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _scope.cancel()
    }
}