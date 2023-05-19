package com.sd.demo.coroutine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sd.demo.coroutine.ui.theme.AppTheme
import com.sd.lib.coroutine.FContinuation
import com.sd.lib.coroutine.FScope
import java.util.UUID

class SampleContinuationActivity : ComponentActivity() {
    private val _scope = FScope()
    private val _continuation = FContinuation<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content(
                    onClickAwait = {
                        _scope.launch {
                            start("launch")
                        }
                    },
                    onClickResumeAwait = {
                        _continuation.resume("hello")
                        logMsg { "FContinuation size:${_continuation.size()}" }
                    },
                    onClickCancelAwait = {
                        _continuation.cancel()
                        logMsg { "FContinuation size:${_continuation.size()}" }
                    },
                    onClickCancel = {
                        _scope.cancel()
                        logMsg { "FContinuation size:${_continuation.size()}" }
                    },
                )
            }
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
            e.printStackTrace()
            throw e
        }

        logMsg { "$tag after $uuid" }
    }

    override fun onDestroy() {
        super.onDestroy()
        _scope.cancel()
    }
}

@Composable
private fun Content(
    onClickAwait: () -> Unit,
    onClickResumeAwait: () -> Unit,
    onClickCancelAwait: () -> Unit,
    onClickCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Button(
            onClick = onClickAwait
        ) {
            Text(text = "await")
        }

        Button(
            onClick = onClickResumeAwait
        ) {
            Text(text = "resumeAwait")
        }

        Button(
            onClick = onClickCancelAwait
        ) {
            Text(text = "cancelAwait")
        }

        Button(
            onClick = onClickCancel
        ) {
            Text(text = "cancel")
        }
    }
}