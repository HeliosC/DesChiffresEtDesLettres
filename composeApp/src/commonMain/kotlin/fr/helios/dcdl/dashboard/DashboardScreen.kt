package fr.helios.dcdl.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import fr.helios.dcdl.game.GameUiState
import fr.helios.dcdl.game.GameViewModel
import fr.helios.dcdl.game.PlayerRoundComponent
import fr.helios.dcdl.game.PlayerScoresComponent
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

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Text("WELCOME IN THE DASHBOARD")
        Text("Game: $gameId")

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

        when {
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
                Row {
                    PlayerScoresComponent(gameUiState.players)
                    Spacer(Modifier.width(64.dp))
                    DashboardRoundComponent(dashboardUiState, gameUiState) { dashboardViewModel.startRound(it) }
                }
            }

            else -> {
                Column(modifier = Modifier) {
                    PlayerScoresComponent(gameUiState.players)
                    Spacer(Modifier.height(64.dp))
                    DashboardRoundComponent(dashboardUiState, gameUiState) { dashboardViewModel.startRound(it) }
                }
            }
        }
    }
}

@Composable
fun DashboardRoundComponent(
    dashboardUiState: DashboardUiState,
    gameUiState: GameUiState,
    startRound: (GameRoundType) -> Unit
) {
    Column(modifier = Modifier) {
        Button(
            enabled = !dashboardUiState.isLoading,
            onClick = {
                startRound(GameRoundType.NUMBERS)
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