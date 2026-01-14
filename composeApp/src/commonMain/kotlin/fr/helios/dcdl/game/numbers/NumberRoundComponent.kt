package fr.helios.dcdl.game.numbers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.model.NumbersOperator
import org.jetbrains.compose.ui.tooling.preview.Preview

interface NumberRoundListener {
    fun onPlayerOperationsChanged(operations: List<NumbersOperationUI>)
}

@Composable
fun NumberRoundComponent(
    roundData: GameRoundData.Numbers,
    isInteractive: Boolean,
    listener: NumberRoundListener,
    numberViewModel: NumberRoundViewModel = viewModel { NumberRoundViewModel() }
) {
    val numberUiState by numberViewModel.uiState.collectAsState()

    LaunchedEffect(numberUiState.operations) {
        listener.onPlayerOperationsChanged(numberUiState.operations)
    }

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.align(Alignment.Start)) {
            NumberObjectiveComponent(roundData.objective)
        }
        Row(Modifier) {
            numberUiState.initialTiles.withIndex().forEach { tile ->
                NumberTitleComponent(
                    modifier = Modifier.width(50.dp),
                    value = tile.value.tile,
                    isUsed = tile.value.isUsed || !isInteractive
                ) {
                    numberViewModel.onClickInitialTile(tile.index)
                }
            }
        }
        Row(Modifier) {
            NumbersOperator.entries.forEach { operator ->
                NumberOperatorComponent(
                    modifier = Modifier.width(50.dp),
                    operator = operator,
                    isInteractive = isInteractive
                ) {
                    numberViewModel.onClickOperator(operator)
                }
            }

            Button(
                modifier = Modifier.numberTilesTheme().width(92.dp),
                enabled = isInteractive,
                onClick = numberViewModel::onDelete
            ) {
                Text("<-")
            }
        }

        numberUiState.operations.withIndex().forEach { operation ->
            NumberOperationComponent(
                modifier = Modifier.align(Alignment.Start).height(50.dp),
                operation = operation.value,
                isInteractive = isInteractive
            ) {
                numberViewModel.onClickResultTile(operation.index)
            }
        }
    }
}

@Composable
fun NumberObjectiveComponent(objective: Int) {
    val style = TextStyle(
        color = Color.Gray,
        background = Color.Cyan,
        fontSize = 32.sp
    )
    Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = "Objectif : $objective",
        style = style
    )
}

@Composable
fun NumberOperationComponent(
    modifier: Modifier,
    operation: NumbersOperationUI,
    isInteractive: Boolean,
    onClickResult: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = operation.number1?.toString() ?: "",
            textAlign = TextAlign.Center,
            modifier = Modifier.width(50.dp)
        )

        Text(
            text = operation.operator?.symbol ?: "",
            textAlign = TextAlign.Center,
            modifier = Modifier.width(100.dp)
        )

        Text(
            text = operation.number2?.toString() ?: "",
            textAlign = TextAlign.Center,
            modifier = Modifier.width(50.dp)
        )

        Text(
            text = if (operation.isFull()) "=" else "",
            textAlign = TextAlign.Center,
            modifier = Modifier.width(50.dp)
        )


        operation.getResult()?.let { result ->
            NumberTitleComponent(
                modifier = Modifier.width(50.dp),
                value = result,
                isUsed = operation.isResultUsed || !isInteractive,
                onClick = onClickResult
            )
        }
    }
}

@Composable
fun NumberTitleComponent(
    modifier: Modifier,
    value: Int,
    isUsed: Boolean,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.numberTilesTheme(),
        enabled = !isUsed,
        onClick = onClick,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = value.toString(),
            maxLines = 1
        )
    }
}

@Composable
fun NumberOperatorComponent(
    modifier: Modifier,
    operator: NumbersOperator,
    isInteractive: Boolean,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.numberTilesTheme(),
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        enabled = isInteractive
    ) {
        Text(
            text = operator.symbol,
            maxLines = 1
        )
    }
}

fun Modifier.numberTilesTheme() = this.
padding(horizontal = 4.dp)


@Preview
@Composable
fun NumberTitlesPreview() {
    NumberRoundComponent(
        GameRoundData.Numbers(
            123,
            listOf(1, 25, 5, 100, 10, 9, 6)
        ),
        isInteractive = true,
        listener = object : NumberRoundListener {
            override fun onPlayerOperationsChanged(operations: List<NumbersOperationUI>) {}
        }
    )
}
