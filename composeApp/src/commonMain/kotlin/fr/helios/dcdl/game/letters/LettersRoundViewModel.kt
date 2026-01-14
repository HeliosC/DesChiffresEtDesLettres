package fr.helios.dcdl.game.letters

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import fr.helios.dcdl.model.GameRoundData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LettersRoundViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(LettersRoundViewModelUiState())
    val uiState = _uiState.asStateFlow()

    fun initWithRoundData(
        roundData: GameRoundData.Letters
    ) {
        _uiState.value = LettersRoundViewModelUiState(roundData)
    }

    fun onClickInitialTile(position: Int) {
        val initialTiles = _uiState.value.initialTiles.toMutableMap()
        val tileUI = initialTiles[position] ?: throw IllegalStateException()
        //if (tileUI.isUsed) return

        val answer = _uiState.value.answer
        val newAnswer = answer + tileUI.id//.tile
        //initialTiles[position] = tileUI.copy(isUsed = true)

        _uiState.value = _uiState.value.copy(
            initialTiles = initialTiles,
            answer = newAnswer,
            draggingLetter = null
        )
    }

    fun onClickAnswerTile(id: Int) {
        val newAnswer = _uiState.value.answer.toMutableList()
        //val tileRemoved =
            newAnswer.remove(id)

        //val initialTiles = _uiState.value.initialTiles.toMutableMap()
        //initialTiles[id] = initialTiles[id]?.copy(isUsed = false) ?: throw IllegalStateException()

        //val initialTileIndex = initialTiles indexOfFirst { it.tile == tileRemoved.tile && it.isUsed }
        //initialTiles[initialTileIndex] = initialTiles[initialTileIndex].copy(isUsed = false)

        _uiState.value = _uiState.value.copy(
            //initialTiles = initialTiles,
            answer = newAnswer,//.joinToString(separator = "")
            draggingLetter = null
        )
    }

    fun updateAnswerTiles(indexToInsert: Int, tileId: Int) {
        val answer = _uiState.value.answer.toMutableList()
        answer.remove(tileId)
        answer.add(indexToInsert.coerceAtMost(answer.size), tileId)
        _uiState.value = _uiState.value.copy(
            answer = answer//.joinToString(separator = "")
        )
    }

    fun removeAnswerTile(tileId: Int) {
        val answer = _uiState.value.answer.toMutableList()
        answer.remove(tileId)
        _uiState.value = _uiState.value.copy(
            answer = answer//.joinToString(separator = "")
        )
    }

    fun updateDraggingLetter(
        draggingLetter: Int? = _uiState.value.draggingLetter,
        draggingLetterOffset: Offset = _uiState.value.draggingLetterOffset
    ) {
        _uiState.value = _uiState.value.copy(
            draggingLetter = draggingLetter,
            draggingLetterOffset = draggingLetterOffset
        )
    }
}

data class LettersRoundViewModelUiState(
    val initialTiles: Map<Int, LettersTileDataUI> = mapOf(),
    //val initialTiles: List<LettersTileDataUI> = listOf(),
    val answer: List<Int> = listOf(),
    val draggingLetter: Int? = null,
    val draggingLetterOffset: Offset = Offset.Zero
) {
    constructor(fromAPI: GameRoundData.Letters): this(
        initialTiles = fromAPI.tiles.withIndex().associate { tile ->
            tile.index to LettersTileDataUI(id = tile.index, tile = tile.value)
        }
    )

    fun inAnswer(tileId: Int) =
        tileId in answer
}