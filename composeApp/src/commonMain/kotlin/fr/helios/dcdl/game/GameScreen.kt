package fr.helios.dcdl.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import fr.helios.dcdl.dashboard.DashboardUiState
import fr.helios.dcdl.dashboard.DashboardViewModel
import fr.helios.dcdl.game.numbers.NumberObjectiveComponent
import fr.helios.dcdl.game.numbers.NumberRoundComponent
import fr.helios.dcdl.game.numbers.NumberRoundListener
import fr.helios.dcdl.game.numbers.NumberRoundViewModel
import fr.helios.dcdl.game.numbers.NumberTitleComponent
import fr.helios.dcdl.game.numbers.NumbersOperationUI
import fr.helios.dcdl.game.numbers.NumbersOperationUI.Companion.toAnswer
import fr.helios.dcdl.model.GameRound
import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.model.GameRoundType
import fr.helios.dcdl.model.NumbersOperation
import fr.helios.dcdl.model.Player
import fr.helios.dcdl.model.PlayerUI
import fr.helios.dcdl.model.RoundAnswer
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.round

@Preview
@Composable
fun GameScreen(
    gameId: String,
    player: PlayerUI,
    gameViewModel: GameViewModel = viewModel { GameViewModel(gameId, player.toPlayerType()) },
    dashboardViewModel: DashboardViewModel = viewModel { DashboardViewModel(gameId) }
) {
    val dashboardUiState by dashboardViewModel.uiState.collectAsState()
    val gameUiState by gameViewModel.uiState.collectAsState()
    val timer by gameViewModel.timer.collectAsState()

    val isAdmin by derivedStateOf { player.id == gameUiState.adminId }

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
            Text("WELCOME IN THE GAME ${player.username}")
            Text("Game: $gameId")
        }

        Spacer(Modifier.height(16.dp))

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        when {
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Column {
                        DashboardActionsComponent(dashboardUiState, isAdmin) { dashboardViewModel.startRound(it) }
                        PlayerScoresComponent(gameUiState.players)
                    }
                    Spacer(Modifier.width(64.dp))
                    PlayerRoundComponent(
                        currentRound = gameUiState.currentRound,
                        roundHistory = gameUiState.rounds,
                        players = gameUiState.players,
                        timer = timer,
                        numberViewModel = numberViewModel
                    ) { answer = it }
                }
            }

            else -> {
                Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    DashboardActionsComponent(dashboardUiState, isAdmin) { dashboardViewModel.startRound(it) }
                    PlayerScoresComponent(gameUiState.players)
                    Spacer(Modifier.height(64.dp))
                    PlayerRoundComponent(
                        currentRound = gameUiState.currentRound,
                        roundHistory = gameUiState.rounds,
                        players = gameUiState.players,
                        timer = timer,
                        numberViewModel = numberViewModel
                    ) { answer = it }
                }
            }
        }
    }
}

@Composable
fun DashboardActionsComponent(
    dashboardUiState: DashboardUiState,
    isAdmin: Boolean,
    startRound: (GameRoundType) -> Unit
) {
    if (!isAdmin) { return }
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
    }
}

@Composable
fun RoundScoresComponent(
    previousRound: GameRound?,
    players: Map<String, Player>
) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (previousRound != null) {
            Text(
                text = "Résultats du round",
                fontSize = 25.sp
            )
        }

        when (previousRound) {
            is GameRound.Numbers -> {
                NumberObjectiveComponent(previousRound.data.objective)

                Row(Modifier) {
                    previousRound.data.tiles.forEach { tile ->
                        NumberTitleComponent(
                            modifier = Modifier.width(50.dp),
                            value = tile,
                            isUsed = false
                        ) { }
                    }
                }

                previousRound.answers
                    .toList()
                    .sortedBy { players.keys.indexOf(it.first) }
                    .forEach{ (playerId, answer) ->

                    Spacer(Modifier.height(50.dp))

                    val player = players[playerId] ?: return@forEach

                    Text(
                        text = "${player.username} : ${answer.result}",
                        fontSize = 25.sp
                    )

                    answer.operation.forEach { operation ->
                        ResultOperationNumberComponent(
                            modifier = Modifier.align(Alignment.Start).height(50.dp),
                            operation = operation
                        )
                    }

                    Spacer(Modifier.height(50.dp))
                }
            }
            is GameRound.Letters -> TODO()
            null -> {
                Text("La partie va commencer")
            }
        }
    }
}

@Composable
fun ResultOperationNumberComponent(
    modifier: Modifier,
    operation: NumbersOperation,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = operation.number1.toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(50.dp)
        )

        Text(
            text = operation.operator.symbol,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(100.dp)
        )

        Text(
            text = operation.number2.toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(50.dp)
        )

        Text(
            text = "=",
            textAlign = TextAlign.Center,
            modifier = Modifier.width(50.dp)
        )

        Text(
            text = operation.getResult().toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(50.dp)
        )
    }
}

@Composable
fun PlayerRoundComponent(
    currentRound: GameRound?,
    roundHistory: List<GameRound>,
    players: Map<String, Player>,
    timer: Long,
    numberViewModel: NumberRoundViewModel,
    onAnswerChanged: (RoundAnswer) -> Unit
) {
    Column(modifier = Modifier) {
        if (currentRound != null) {
            Text("Time Left: ${timer.div(1000).toInt()}")
        }

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
                RoundScoresComponent(
                    previousRound = roundHistory.lastOrNull(),
                    players = players
                )
            }
        }

        if (currentRound != null && timer == 0L) {
            Text("FIN DU ROUND")
        }
    }
}

@Composable
fun PlayerScoresComponent(players: Map<String, Player>) {
    Column {
        Text("Tableau des scores")
        players.values.forEach { (_, username, score) ->
            Text("$username: $score")
        }
    }
}
