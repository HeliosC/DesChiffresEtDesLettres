package fr.helios.dcdl.game.numbers

import androidx.lifecycle.ViewModel
import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.model.NumbersOperator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NumberRoundViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(NumberRoundViewModelUiState())
    val uiState = _uiState.asStateFlow()

    fun initWithRoundData(
        roundData: GameRoundData.Numbers
    ) {
        _uiState.value = NumberRoundViewModelUiState(roundData)
    }

    fun onClickInitialTile(position: Int) {
        val operations = _uiState.value.operations.toMutableList()
        val initialTiles = _uiState.value.initialTiles.toMutableList()
        val tileUI = initialTiles[position]
        if (tileUI.isUsed) return

        val lastOperation = operations.lastOrNull() ?: NumbersOperationUI().also { operations.add(it) }
        if (lastOperation.isFull()) {
            operations.add(NumbersOperationUI(number1 = tileUI.tile))
            initialTiles[position] = tileUI.copy(isUsed = true)
            _uiState.value = _uiState.value.copy(
                initialTiles = initialTiles,
                operations = operations
            )
        } else { //last operation not full
            val (newOperation, couldBeAdded) = lastOperation.addNumberIfSlotEmpty(tileUI.tile)
            if (!couldBeAdded) return

            operations[operations.lastIndex] = newOperation
            initialTiles[position] = tileUI.copy(isUsed = true)
            _uiState.value = _uiState.value.copy(
                initialTiles = initialTiles,
                operations = operations
            )
        }
    }

    fun onClickResultTile(position: Int) {
        val operations = _uiState.value.operations.toMutableList()
        val operation = operations[position]
        if (operation.isResultUsed) return

        //if all tile used
        if (_uiState.value.initialTiles.all { it.isUsed }
            && operations.dropLast(1).all { it.isResultUsed }) return

        val lastOperation = operations.lastOrNull() ?: NumbersOperationUI().also { operations.add(it) }
        val clickedResult = operation.getResult() ?: return
        if (lastOperation.isFull()) {
            operations.add(NumbersOperationUI(number1 = clickedResult))
            operations[position] = operation.copy(isResultUsed = true)
            _uiState.value = _uiState.value.copy(
                operations = operations
            )
        } else { //last operation not full
            val (newOperation, couldBeAdded) = lastOperation.addNumberIfSlotEmpty(clickedResult)
            if (!couldBeAdded) return

            operations[operations.lastIndex] = newOperation
            operations[position] = operation.copy(isResultUsed = true)
            _uiState.value = _uiState.value.copy(
                operations = operations
            )
        }
    }

    fun onClickOperator(operator: NumbersOperator) {
        val operations = _uiState.value.operations.toMutableList()
        val lastOperation = operations.lastOrNull() ?: return

        val (newOperation, couldBeEdited) = lastOperation.editOperator(operator)
        if (!couldBeEdited) return

        operations[operations.lastIndex] = newOperation
        _uiState.value = _uiState.value.copy(
            operations = operations
        )
    }

    fun onDelete() {
        fun onRestoreTile(
            tile: Int,
            initialTiles: MutableList<NumbersTileDataUI>,
            operations: MutableList<NumbersOperationUI>
        ): Pair<List<NumbersTileDataUI>, List<NumbersOperationUI>> {
            //in result tile
            val resultTileIndex = operations.indexOfLast { it.getResult() == tile && it.isResultUsed }
            if (resultTileIndex != -1) {
                operations[resultTileIndex] = operations[resultTileIndex].copy(isResultUsed = false)
                return initialTiles to operations
            }

            //in initial tile
            val initialTileIndex = initialTiles.indexOfLast { it.tile == tile && it.isUsed }
            if (initialTileIndex != -1) {
                initialTiles[initialTileIndex] = initialTiles[initialTileIndex].copy(isUsed = false)
                return initialTiles to operations
            }

            return initialTiles to operations
        }

        var initialTiles = _uiState.value.initialTiles.toMutableList()
        var operations = _uiState.value.operations.toMutableList()
        val lastOperation = operations.lastOrNull() ?: return

        val (number1, number2) = lastOperation.number1 to lastOperation.number2
        if (number2 != null) {
            operations[operations.lastIndex] = lastOperation.copy(number2 = null)
            onRestoreTile(number2, initialTiles, operations).let { (newInitialTiles, newOperations) ->
                initialTiles = newInitialTiles.toMutableList()
                operations = newOperations.toMutableList()
            }
        } else if (lastOperation.operator != null) {
            operations[operations.lastIndex] = lastOperation.copy(operator = null)
        } else if (number1 != null) {
            operations[operations.lastIndex] = lastOperation.copy(number1 = null)
            onRestoreTile(number1, initialTiles, operations).let { (newInitialTiles, newOperations) ->
                initialTiles = newInitialTiles.toMutableList()
                operations = newOperations.toMutableList()
            }
        }

        if (operations[operations.lastIndex].isEmpty()) {
            operations.removeAt(operations.lastIndex)
        }

        _uiState.value = _uiState.value.copy(
            initialTiles = initialTiles,
            operations = operations
        )
    }
}

data class NumberRoundViewModelUiState(
    val initialTiles: List<NumbersTileDataUI> = listOf(),
    val operations: List<NumbersOperationUI> = listOf()
) {
    constructor(fromAPI: GameRoundData.Numbers): this(
        initialTiles = fromAPI.tiles.map { NumbersTileDataUI(it) }
    )
}