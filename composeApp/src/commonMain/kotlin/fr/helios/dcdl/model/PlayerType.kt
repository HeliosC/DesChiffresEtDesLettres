package fr.helios.dcdl.model

sealed interface PlayerType {
    val isAdmin: Boolean

    object Admin: PlayerType {
        override val isAdmin: Boolean = true
    }

    data class Player(val username: String): PlayerType {
        override val isAdmin: Boolean = false
    }
}