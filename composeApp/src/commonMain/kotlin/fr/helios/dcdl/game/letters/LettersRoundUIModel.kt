package fr.helios.dcdl.game.letters

data class LettersTileDataUI(
    val id: Int,
    val tile: Char
)

enum class LetterState {
    IDLE,
    DRAGGING,
    DISABLED
}
