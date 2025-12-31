package fr.helios.dcdl.model

import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: String,
    val state: GameState = GameState.WAITING,
    val players: List<Player> = emptyList()
)

@Serializable
enum class GameState {
    WAITING, IN_GAME, FINISHED
}

@Serializable
data class Player(
    val username: String,
    val score: Int = 0
)