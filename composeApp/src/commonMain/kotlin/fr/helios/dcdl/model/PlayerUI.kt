package fr.helios.dcdl.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerUI(
    val id: String,
    val username: String
) {
    fun toPlayerType() = PlayerType.Player(
        id = id,
        username = username
    )
}

sealed interface PlayerType {
    val id: String

    class Admin(
        override val id: String
    ): PlayerType

    data class Player(
        override val id: String,
        val username: String
    ): PlayerType
}
