package fr.helios.dcdl.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.helios.dcdl.model.ClientWsMessage
import fr.helios.dcdl.model.ClientWsMessageData
import fr.helios.dcdl.model.Game
import fr.helios.dcdl.model.GameRound
import fr.helios.dcdl.model.GameState
import fr.helios.dcdl.model.Player
import fr.helios.dcdl.model.PlayerType
import fr.helios.dcdl.model.RoundAnswer
import fr.helios.dcdl.network.GameApi
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock

interface GameListener {
    fun getAnswer(): RoundAnswer?
}

class GameViewModel(
    private val gameId: String,
    private val player: PlayerType
) : ViewModel() {
    private var listener: GameListener? = null
    fun setListener(listener: GameListener) {
        this.listener = listener
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    private val _timer = MutableStateFlow(0L)
    val timer = _timer.asStateFlow()

    private var timerJob: Job? = null
    private var wsSession: DefaultClientWebSocketSession? = null

    init {
        connectToWabSocket()
    }

    fun connectToWabSocket() {
        viewModelScope.launch {
            GameApi.connectToWebSocket(
                gameId,
                onSessionCreated = { wsSession = it },
                onSessionEnded = { wsSession = null }
            ) { game ->
                handleNewGameState(game)
            }
        }
    }

    fun handleNewGameState(game: Game) {
        _uiState.value = GameUiState(game)

        val currentRound = game.currentRound
        if (currentRound != null) {
            startCountDown(startTime = currentRound.startTime, roundDuration = currentRound.data.type.duration)
        } else {
            stopCountDown()
        }
    }

    fun startCountDown(startTime: Long, roundDuration: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var timeRemaining = startTime + roundDuration - Clock.System.now().toEpochMilliseconds()

            //TODO: better clock
            while (timeRemaining > 0) {
                _timer.value = timeRemaining
                delay(100)
                timeRemaining = startTime + roundDuration - Clock.System.now().toEpochMilliseconds()
            }
            _timer.value = 0

            submitAnswer()
        }
    }

    fun stopCountDown() {
        timerJob?.cancel()
        _timer.value = 0
    }

    fun submitAnswer() {
        if (player !is PlayerType.Player) return
        listener?.let { listener ->

            viewModelScope.launch {
                val message = ClientWsMessageData.SubmitResponse(
                    username = player.username,
                    answer = listener.getAnswer()
                )

                try {
                    wsSession?.sendSerialized(ClientWsMessage(message))
                } catch (e: Exception) {
                    println("ERROR submitAnswer ${player.username} - $e")
                    //TODO: submit error (UI)
                }
            }
        }
    }
}

//TODO: handleError with interface
data class GameUiState(
    val state: GameState = GameState.WAITING,
    val players: List<Player> = emptyList(),
    val currentRound: GameRound? = null
) {
    constructor(fromApi: Game): this(
        state = fromApi.state,
        players = fromApi.players,
        currentRound = fromApi.currentRound
    )
}