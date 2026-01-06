package fr.helios.dcdl.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.helios.dcdl.dto.GameStartRoundResponse
import fr.helios.dcdl.model.GameRoundType
import fr.helios.dcdl.network.GameApi
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(private val gameId: String): ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    fun startRound(roundType: GameRoundType) {
        _uiState.value = _uiState.value.copy(
            launchedRound = roundType,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val response = GameApi.startRound(gameId = gameId, roundType = roundType)

            try {
                val data = response.body<GameStartRoundResponse>()

                _uiState.value = DashboardUiState(
                    launchedRound = roundType,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                println("LAUNCH ROUND ERROR: $e")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = response.bodyAsText()
                )
            }
        }
    }
}

data class DashboardUiState(
    val launchedRound: GameRoundType? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)