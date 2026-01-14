package fr.helios.dcdl.game.letters

data class LettersTileDataUI(
    val id: Int,
    val tile: Char,
    //val isUsed: Boolean = false //TODO remove and base on uiState
)

enum class LetterState {
    IDLE,
    DRAGGING,
    DISABLED
}