package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import org.example.project.navigation.NavGraph
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavGraph(navController = navController)
    }
}