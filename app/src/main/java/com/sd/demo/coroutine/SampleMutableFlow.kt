package com.sd.demo.coroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutine.databinding.SampleMutableFlowBinding
import com.sd.lib.coroutine.FMutableFlowStore
import com.sd.lib.coroutine.FScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SampleMutableFlow : AppCompatActivity() {
    private val _binding by lazy { SampleMutableFlowBinding.inflate(layoutInflater) }

    private val _scope = FScope()
    private val _store = FMutableFlowStore<Int, MutableStateFlow<Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnCollect.setOnClickListener {
            val flow = _store.get("") { MutableStateFlow(0) }
            collectFlow(flow)
            logMsg { "flow subscriptionCount:${flow.subscriptionCount}" }
        }

        _binding.btnCancel.setOnClickListener {
            _scope.cancel()
        }

        _binding.btnLog.setOnClickListener {
            logMsg { "store size:${_store.size()}" }
        }
    }

    private fun collectFlow(flow: Flow<Int>) {
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