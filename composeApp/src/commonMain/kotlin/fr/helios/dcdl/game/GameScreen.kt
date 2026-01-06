package fr.helios.dcdl.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.helios.dcdl.game.numbers.NumberRoundComponent
import fr.helios.dcdl.game.numbers.NumberRoundListener
import fr.helios.dcdl.game.numbers.NumbersOperationUI
import fr.helios.dcdl.game.numbers.NumbersOperationUI.Companion.toAnswer
import fr.helios.dcdl.model.GameRoundData
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

    LaunchedEffect(gameUiState.currentRound?.data) {
        answer = null
    }

    Row(modifier = Modifier) {
        //Players
        Column {
            Text("Tableau des scores")
            gameUiState.players.forEach { (username, score) ->
                Text("$username: $score")
            }
        }

        Spacer(Modifier.width(64.dp))

        Column(modifier = Modifier) {
            Text("WELCOME IN THE GAME $username")
            Text("Game: $gameId")
            Text("Time Left: $timer")

            when (val currentRound = gameUiState.currentRound?.data) {
                is GameRoundData.Numbers -> {
                    NumberRoundComponent(
                        roundData = currentRound,
                        isInteractive = timer > 0,
                        listener = object : NumberRoundListener {
                            override fun getPlayerOperation(operations: List<NumbersOperationUI>) {
                                answer = operations.toAnswer()
                            }
                        }
                    )
                }

                is GameRoundData.Letters -> TODO()

                null -> {
                    Text("PAS DE ROUND EN COURS")
                }
            }

            if (gameUiState.currentRound != null && timer == 0L) {
                Text("FIN DU ROUND")
            }
        }
    }
}