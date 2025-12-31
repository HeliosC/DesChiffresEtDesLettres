package fr.helios.dcdl.dto

import fr.helios.dcdl.model.Game
import kotlinx.serialization.Serializable

@Serializable
data class GameCreateRequest(val gameId: String)

@Serializable
data class GameCreateResponse(val game: Game)

@Serializable
data class GameJoinRequest(val gameId: String, val username: String)

@Serializable
data class GameJoinResponse(val game: Game) //success, message?