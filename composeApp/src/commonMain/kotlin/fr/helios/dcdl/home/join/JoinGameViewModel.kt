package fr.helios.dcdl.home.join

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.helios.dcdl.MAX_CUSTOM_ID_LENGTH
import fr.helios.dcdl.dto.GameJoinResponse
import fr.helios.dcdl.home.create.CreateGameUiState
import fr.helios.dcdl.network.GameApi
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JoinGameViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(JoinGameUiState())
    val uiState = _uiState.asStateFlow()

    fun joinGame(gameId: String, userId: String, username: String) {
        if (userId.length > MAX_CUSTOM_ID_LENGTH) {
            _uiState.value = JoinGameUiState(
                isLoading = false,
                error = "10 char max",
                successOrNull = null
            )

            return
        }

        _uiState.value = JoinGameUiState(
            isLoading = true,
            error = null,
            successOrNull = null
        )

        viewModelScope.launch {
            val response = GameApi.join(gameId = gameId, userId = userId, username = username)

            try {
                val data = response.body<GameJoinResponse>()

                _uiState.value = JoinGameUiState(
                    isLoading = false,
                    error = null,
                    successOrNull = JoinGameUiSuccess(
                        joinedGameId = data.game.id,
                        username = username
                    )
                )
            } catch (e: Exception) {
                println("JOIN GAME ERROR: $e")
                _uiState.value = JoinGameUiState(
                    isLoading = false,
                    error = response.bodyAsText(),
                    successOrNull = null
                )
            }
        }
    }
}

data class JoinGameUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successOrNull: JoinGameUiSuccess? = null,
)

data class JoinGameUiSuccess(
    val joinedGameId: String,
    val username: String
)
