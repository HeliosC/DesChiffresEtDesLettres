package fr.helios.dcdl.home.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.helios.dcdl.dto.GameCreateResponse
import fr.helios.dcdl.network.GameApi
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateGameViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(CreateGameUiState())
    val uiState = _uiState.asStateFlow()

    fun createGame(gameId: String) {
        _uiState.value = CreateGameUiState(
            isLoading = true,
            error = null,
            successOrNull = null
        )

        viewModelScope.launch {
            val response = GameApi.create(gameId)

            try {
                val data = response.body<GameCreateResponse>()

                _uiState.value = CreateGameUiState(
                    isLoading = false,
                    error = null,
                    successOrNull = CreateGameUiSuccess(data.game.id)
                )
            } catch (e: Exception) {
                println("CREATE GAME ERROR: $e")
                _uiState.value = CreateGameUiState(
                    isLoading = false,
                    error = response.bodyAsText(),
                    successOrNull = null
                )
            }
        }
    }
}

data class CreateGameUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successOrNull: CreateGameUiSuccess? = null,
)

data class CreateGameUiSuccess(
    val createdGameId: String
)