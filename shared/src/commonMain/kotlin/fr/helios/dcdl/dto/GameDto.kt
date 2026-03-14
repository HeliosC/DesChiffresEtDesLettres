package fr.helios.dcdl.dto

import fr.helios.dcdl.model.Game
import fr.helios.dcdl.model.GameRoundType
import kotlinx.serialization.Serializable

@Serializable
data class GameCreateRequest(val gameId: String, val adminId: String)

@Serializable
data class GameCreateResponse(val game: Game)

@Serializable
data class GameJoinRequest(val gameId: String, val userId: String, val username: String)

@Serializable
data class GameJoinResponse(val game: Game) //success, message?

@Serializable
data class GameStartRoundRequest(val gameId: String, val roundType: GameRoundType)

@Serializable
data class GameStartRoundResponse(val game: Game)
