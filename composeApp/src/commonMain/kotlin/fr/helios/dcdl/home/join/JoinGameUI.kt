package fr.helios.dcdl.home.join

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
fun JoinGameUI(
    navigateToGame: (String, String) -> Unit,
    viewModel: JoinGameViewModel = viewModel { JoinGameViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        Text("Join a game")

        val joinGameIdState = rememberTextFieldState()
        TextField(
            state = joinGameIdState,
            label = { Text("Game ID") }
        )

        val joinUsernameState = rememberTextFieldState()
        TextField(
            state = joinUsernameState,
            label = { Text("Username") }
        )

        Button(onClick = {
            viewModel.joinGame(gameId = joinGameIdState.text.toString(), username = joinUsernameState.text.toString())
        }) {
            Text("Join !")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.error?.let { error ->
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        }

        uiState.successOrNull?.let { successData ->
            LaunchedEffect(Unit) {
                navigateToGame.invoke(
                    successData.joinedGameId,
                    successData.username
                )
            }
        }
    }
}