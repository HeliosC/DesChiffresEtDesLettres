package fr.helios.dcdl.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import fr.helios.dcdl.game.numbers.NumberRoundComponent
import fr.helios.dcdl.game.numbers.NumberRoundListener
import fr.helios.dcdl.game.numbers.NumberRoundViewModel
import fr.helios.dcdl.game.numbers.NumbersOperationUI
import fr.helios.dcdl.game.numbers.NumbersOperationUI.Companion.toAnswer
import fr.helios.dcdl.model.GameRound
import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.model.Player
import fr.helios.dcdl.model.PlayerType
import fr.helios.dcdl.model.RoundAnswer
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun GameScreen(
    gameId: String,
    username: String,
    gameViewModel: GameViewModel = viewModel { GameViewModel(gameId, PlayerType.Player(username)) },
) {
    val gameUiState by gameViewModel.uiState.collectAsState()
    val timer by gameViewModel.timer.collectAsState()

    var answer: RoundAnswer? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        gameViewModel.setListener(object : GameListener {
            override fun getAnswer(): RoundAnswer? = answer
        })
    }

    val numberViewModel: NumberRoundViewModel = viewModel { NumberRoundViewModel() }

    LaunchedEffect(gameUiState.currentRound?.data) {
        answer = null

        when (val roundData = gameUiState.currentRound?.data) {
            is GameRoundData.Numbers -> numberViewModel.initWithRoundData(roundData)
            is GameRoundData.Letters -> TODO()
            null -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(Modifier.align (Alignment.CenterHorizontally)) {
            Text("WELCOME IN THE GAME $username")
            Text("Game: $gameId")
        }

        Spacer(Modifier.height(16.dp))

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        when {
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    PlayerScoresComponent(gameUiState.players)
                    Spacer(Modifier.width(64.dp))
                    PlayerRoundComponent(gameUiState.currentRound, timer, numberViewModel) { answer = it }
                }
            }

            else -> {
                Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    PlayerScoresComponent(gameUiState.players)
                    Spacer(Modifier.height(64.dp))
                    PlayerRoundComponent(gameUiState.currentRound, timer, numberViewModel) { answer = it }
                }
            }
        }
    }
}

@Composable
fun PlayerRoundComponent(
    currentRound: GameRound?,
    timer: Long,
    numberViewModel: NumberRoundViewModel,
    onAnswerChanged: (RoundAnswer) -> Unit
) {
    Column(modifier = Modifier) {
        Text("Time Left: $timer")

        when (val currentRound = currentRound?.data) {
            is GameRoundData.Numbers -> {
                NumberRoundComponent(
                    roundData = currentRound,
                    isInteractive = timer > 0,
                    listener = object : NumberRoundListener {
                        override fun onPlayerOperationsChanged(operations: List<NumbersOperationUI>) {
                            onAnswerChanged(operations.toAnswer())
                        }
                    },
                    numberViewModel
                )
            }

            is GameRoundData.Letters -> TODO()

            null -> {
                Text("PAS DE ROUND EN COURS")
            }
        }

        if (currentRound != null && timer == 0L) {
            Text("FIN DU ROUND")
        }
    }
}

@Composable
fun PlayerScoresComponent(players: List<Player>) {
    Column {
        Text("Tableau des scores")
        players.forEach { (username, score) ->
            Text("$username: $score")
        }
    }
}