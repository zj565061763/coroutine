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
import com.sd.lib.coroutine.FMutator
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class SampleMutatorActivity : ComponentActivity() {
    private val _scope = MainScope()
    private val _mutator = FMutator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content(
                    onClickMutate1 = {
                        _scope.launch {
                            _mutator.mutate {
                                start("mutate_1")
                            }
                        }
                    },
                    onClickMutate2 = {
                        _scope.launch {
                            _mutator.mutate(priority = 1) {
                                start("mutate_2")
                            }
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
    onClickMutate1: () -> Unit,
    onClickMutate2: () -> Unit,
    onClickCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Button(
            onClick = onClickMutate1
        ) {
            Text(text = "mutate1")
        }

        Button(
            onClick = onClickMutate2
        ) {
            Text(text = "mutate2")
        }

        Button(
            onClick = onClickCancel
        ) {
            Text(text = "cancel")
        }
    }
}