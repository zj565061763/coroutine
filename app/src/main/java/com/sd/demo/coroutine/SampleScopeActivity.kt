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
import com.sd.lib.coroutine.FScope
import kotlinx.coroutines.delay
import java.util.UUID

class SampleScopeActivity : ComponentActivity() {
    private val _scope = FScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content(
                    onClickLaunch = {
                        _scope.launch {
                            start("launch")
                        }
                    },
                    onClickCancel = {
                        _scope.cancel()
                    },
                )
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

@Composable
private fun Content(
    onClickLaunch: () -> Unit,
    onClickCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Button(
            onClick = onClickLaunch
        ) {
            Text(text = "launch")
        }

        Button(
            onClick = onClickCancel
        ) {
            Text(text = "cancel")
        }
    }
}