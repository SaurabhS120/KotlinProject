package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DetailsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .safeContentPadding()
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Details Screen",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Details Information",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This is the details screen. You can display more information here.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go Back")
            }
        }
    }
}

@Preview
@Composable
fun DetailsScreenPreview() {
    MaterialTheme {
        DetailsScreen(onNavigateBack = {})
    }
}
