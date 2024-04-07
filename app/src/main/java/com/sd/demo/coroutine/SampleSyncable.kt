package com.sd.demo.coroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.coroutine.databinding.SampleSyncableBinding
import com.sd.lib.coroutine.FSyncable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class SampleSyncable : AppCompatActivity() {
    private val _binding by lazy { SampleSyncableBinding.inflate(layoutInflater) }

    private val _syncable = FSyncable(lifecycleScope) { loadData() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnSync.setOnClickListener {
            _syncable.sync()
        }
        _binding.btnSyncAwait.setOnClickListener {
            lifecycleScope.launch {
                syncAwait()
            }
        }
    }

    private suspend fun syncAwait() {
        val uuid = UUID.randomUUID().toString()
        logMsg { "syncAwait start $uuid" }

        val result = try {
            _syncable.syncAwait()
        } catch (e: Throwable) {
            logMsg { "syncAwait error:$e $uuid" }
            throw e
        }

        result.onSuccess {
            logMsg { "syncAwait onSuccess $it $uuid" }
        }
        result.onFailure {
            logMsg { "syncAwait onFailure $it $uuid" }
        }
    }

    private suspend fun loadData(): Int {
        logMsg { "loadData start" }
        try {
            delay(10_000)
        } catch (e: Throwable) {
            logMsg { "loadData error:$e" }
            throw e
        }
        logMsg { "loadData finish" }
        return 1
    }
}