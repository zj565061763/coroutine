package com.sd.demo.coroutine

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content(
                    onClickScope = {
                        startActivity(Intent(this, SampleScopeActivity::class.java))
                    },
                    onClickMutator = {
                        startActivity(Intent(this, SampleMutatorActivity::class.java))
                    },
                    onClickContinuation = {
                        startActivity(Intent(this, SampleContinuationActivity::class.java))
                    },
                )
            }
        }
    }
}

@Composable
private fun Content(
    onClickScope: () -> Unit,
    onClickMutator: () -> Unit,
    onClickContinuation: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Button(
            onClick = onClickScope
        ) {
            Text(text = "scope")
        }

        Button(
            onClick = onClickMutator
        ) {
            Text(text = "mutator")
        }

        Button(
            onClick = onClickContinuation
        ) {
            Text(text = "continuation")
        }
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("coroutine-demo", block())
}