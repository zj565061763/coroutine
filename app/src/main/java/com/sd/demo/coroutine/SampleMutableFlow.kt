package com.sd.demo.coroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.coroutine.databinding.SampleMutableFlowBinding
import com.sd.lib.coroutine.FMutableFlowStore
import com.sd.lib.coroutine.FScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class SampleMutableFlow : AppCompatActivity() {
    private val _binding by lazy { SampleMutableFlowBinding.inflate(layoutInflater) }

    private val _scope = FScope()
    private val _store = FMutableFlowStore<MutableSharedFlow<Int>>()

    private val _flow: MutableSharedFlow<Int>
        get() = _store.get("") {
            MutableSharedFlow<Int>(replay = 1).apply { tryEmit(1) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnCollect.setOnClickListener {
            collectFlow(_flow)
        }

        _binding.btnCancelCollect.setOnClickListener {
            _scope.cancel()
        }

        _binding.btnEmit.setOnClickListener {
            _flow.tryEmit(0)
        }

        _binding.btnClearStore.setOnClickListener {
            _store.clear()
        }

        _binding.btnLog.setOnClickListener {
            logMsg { "store size:${_store.size()} collect size:${_scope.size()}" }
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