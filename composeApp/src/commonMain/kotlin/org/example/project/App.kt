package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    // Step 1: Create a SnackbarHostState to control snackbar visibility
    val snackbarHostState = remember { SnackbarHostState() }

    // Step 2: Get coroutine scope (required to show snackbar)
    val scope = rememberCoroutineScope()
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ){
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = { showContent = !showContent }) {
                    Text("Click me!")
                }
                AnimatedVisibility(showContent) {
                    val greeting = remember { Greeting().greet() }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(painterResource(Res.drawable.compose_multiplatform), null)
                        Text("Compose: $greeting")
                    }
                }
                OutlinedButton(
                    onClick = {

                        // Step 4: Show snackbar inside coroutine
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Hello from Snackbar!",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )

//                            // Step 5: Handle user action (optional)
//                            when (result) {
//                                SnackbarResult.ActionPerformed -> Log.d("Snackbar", "Undo clicked")
//                                SnackbarResult.Dismissed -> Log.d("Snackbar", "Dismissed")
//                            }
                        }
                    }
                ) {
                    Text("Outlined Button")
                }
            }
        }
    }
}