package fr.helios.dcdl.home.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun CreateGameUI(
    navigateToDashboard: (String) -> Unit,
    viewModel: CreateGameViewModel = viewModel { CreateGameViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        Text("Create a game")

        val createGameIdState = rememberTextFieldState()
        TextField(
            state = createGameIdState,
            label = { Text("Game ID") }
        )

        Button(onClick = {
            viewModel.createGame(createGameIdState.text.toString())
        }) {
            Text("Create !")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.error?.let { error ->
            Text(": $error", color = MaterialTheme.colorScheme.error)
        }

        uiState.successOrNull?.let { successData ->
            LaunchedEffect(Unit) {
                navigateToDashboard.invoke(successData.createdGameId)
            }
        }
    }
}