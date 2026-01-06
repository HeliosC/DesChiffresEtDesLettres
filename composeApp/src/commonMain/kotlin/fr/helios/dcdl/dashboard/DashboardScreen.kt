package fr.helios.dcdl.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.helios.dcdl.game.GameViewModel
import fr.helios.dcdl.game.numbers.NumberObjectiveComponent
import fr.helios.dcdl.game.numbers.NumberTitleComponent
import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.model.GameRoundType
import fr.helios.dcdl.model.PlayerType
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun DashboardScreen(
    gameId: String,
    gameViewModel: GameViewModel = viewModel { GameViewModel(gameId, PlayerType.Admin) },
    dashboardViewModel: DashboardViewModel = viewModel { DashboardViewModel(gameId) }
) {
    val gameUiState by gameViewModel.uiState.collectAsState()
    val dashboardUiState by dashboardViewModel.uiState.collectAsState()

    Row {
        Column {
            Text("Tableau des scores")
            gameUiState.players.forEach { (username, score) ->
                Text("$username: $score")
            }
        }

        Spacer(Modifier.width(64.dp))

        Column(modifier = Modifier) {
            Text("WELCOME IN THE DASHBOARD")
            Text("Game: $gameId")

            Button(
                enabled = !dashboardUiState.isLoading,
                onClick = {
                    dashboardViewModel.startRound(GameRoundType.NUMBERS)
                }
            ) {
                Text("Lancer Round Chiffres")
            }

            if (dashboardUiState.isLoading) {
                CircularProgressIndicator()
            }
            dashboardUiState.error?.let { error ->
                Text(": $error", color = MaterialTheme.colorScheme.error)
            }

            when (val round = gameUiState.currentRound?.data) {
                is GameRoundData.Numbers -> {
                    NumberObjectiveComponent(round.objective)

                    Row(Modifier.fillMaxWidth()) {
                        round.tiles.forEach { tile ->
                            NumberTitleComponent(
                                modifier = Modifier.width(50.dp),
                                value = tile,
                                isUsed = false
                            ) { }
                        }
                    }
                }
                is GameRoundData.Letters -> TODO()
                null -> {}
            }
        }
    }
}