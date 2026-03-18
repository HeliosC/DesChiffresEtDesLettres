package fr.helios.dcdl.game.letters

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.util.OffsetExt.toIntOffset
import org.jetbrains.compose.ui.tooling.preview.Preview

interface LettersRoundListener {
    fun onPlayerAnswerChanged(answer: String)
}

@Composable
fun LettersRoundComponent(
    isInteractive: Boolean,
    listener: LettersRoundListener,
    lettersViewModel: LettersRoundViewModel = viewModel { LettersRoundViewModel() }
) {
    val lettersUiState by lettersViewModel.uiState.collectAsState()

    LaunchedEffect(lettersUiState.answer) {
        listener.onPlayerAnswerChanged(lettersUiState.answer.joinToString(separator = "") { id ->
            lettersUiState.initialTiles[id]?.tile?.toString() ?: ""
        })
    }

    LaunchedEffect(isInteractive) {
        if (!isInteractive) {
            lettersViewModel.updateDraggingLetter(draggingLetter = null)
        }
    }

    val availablePositionRoot = remember { mutableStateMapOf<Int, Offset>() }
    val selectedPositionRoot = remember { mutableStateMapOf<Int, Offset>() }

    var selectedRowBounds by remember { mutableStateOf(Rect.Zero) }

    Column(modifier =
        Modifier.
        pointerInput(isInteractive) {
            if (!isInteractive) return@pointerInput

            detectDragGestures(
                onDragStart = {},
                onDrag = { a, b -> lettersViewModel.getOnLetterDrag(selectedRowBounds).invoke(a, b, this) },
                onDragEnd = { lettersViewModel.updateDraggingLetter(draggingLetter = null) },
                onDragCancel = { lettersViewModel.updateDraggingLetter(draggingLetter = null) },
            )
        }
    ) {

        LetterInitialTilesComponent(
            onLetterGloballyPositioned = remember { { tile ->
                { coordinates -> availablePositionRoot[tile] = coordinates.positionInRoot() }
            } },
            dragEvents = remember { object: GetLetterDragEvents {
                override fun getOnDragStart(tileId: Int) =
                    lettersViewModel.getOnLetterDragStart(tileId) { tileId: Int -> availablePositionRoot[tileId] ?: Offset.Zero }
                override fun getOnDrag() =
                    lettersViewModel.getOnLetterDrag(selectedRowBounds)
                override fun getOnDragEnd() = { lettersViewModel.updateDraggingLetter(draggingLetter = null) }
                override fun getOnDragCancel() = { lettersViewModel.updateDraggingLetter(draggingLetter = null) }
            } },
            uiState = lettersUiState,
            isInteractive = isInteractive
        ) { lettersViewModel.onClickInitialTile(it) }

        Spacer(Modifier.height(8.dp))

        LetterAnswerTilesComponent(
            onGloballyPositioned = remember { { coordinates ->
                selectedRowBounds = coordinates.boundsInRoot()
            } },
            onLetterGloballyPositioned = remember { { tile ->
                { coordinates -> selectedPositionRoot[tile] = coordinates.positionInRoot() }
            } },
            dragEvents = remember { object: GetLetterDragEvents {
                override fun getOnDragStart(tileId: Int) =
                    lettersViewModel.getOnLetterDragStart(tileId) { tileId: Int -> selectedPositionRoot[tileId] ?: Offset.Zero }
                override fun getOnDrag() =
                        lettersViewModel.getOnLetterDrag(selectedRowBounds)
                override fun getOnDragEnd() = { lettersViewModel.updateDraggingLetter(draggingLetter = null) }
                override fun getOnDragCancel() = { } //so the column keep the track of the drag when the letter is remove from the answer
            } },
            uiState = lettersUiState,
            isInteractive = isInteractive
        ) { lettersViewModel.onClickAnswerTile(it) }


        var draggingLetterPosition by remember { mutableStateOf(Offset.Zero) }

        lettersUiState.initialTiles[lettersUiState.draggingLetter]?.let { draggingLetter ->
            LetterTitleComponent(
                modifier = {
                    this.offset { (lettersUiState.draggingLetterOffset - draggingLetterPosition).toIntOffset() }
                        .zIndex(1f)
                },
                onGloballyPositioned = remember { { coordinates ->
                    draggingLetterPosition = coordinates.positionInRoot()
                } },
                value = draggingLetter.tile,
                state = LetterState.IDLE,
                dragEvents = null,
                onClick = {}
            )
        }
    }
}

@Composable
fun LetterInitialTilesComponent(
    modifier: Modifier = Modifier,
    letterModifier: Modifier.() -> Modifier = { this },
    onLetterGloballyPositioned: (Int) -> ((LayoutCoordinates) -> Unit),
    dragEvents: GetLetterDragEvents,
    uiState: LettersRoundViewModelUiState,
    isInteractive: Boolean,
    onClick: (Int) -> Unit
) {
    Row(
        modifier.fillMaxWidth().height(50.dp).border(width = 3.dp, color = Color.Green)
    ) {
        uiState.initialTiles.values.sortedBy { it.id }.forEach { tile ->
            LetterTitleComponent(
                modifier = letterModifier,
                onGloballyPositioned = onLetterGloballyPositioned.invoke(tile.id),
                value = tile.tile,
                state = when {
                    !isInteractive -> LetterState.DISABLED
                    uiState.draggingLetter == tile.id -> LetterState.DRAGGING
                    uiState.inAnswer(tile.id) -> LetterState.DISABLED
                    else -> LetterState.IDLE
                },
                dragEvents = dragEvents.toEvents(tile.id),
                onClick = { onClick.invoke(tile.id) }
            )
        }
    }
}

@Composable
fun LetterAnswerTilesComponent(
    modifier: Modifier = Modifier,
    onGloballyPositioned: (LayoutCoordinates) -> Unit,
    letterModifier: Modifier.() -> Modifier = { this },
    onLetterGloballyPositioned: (Int) -> ((LayoutCoordinates) -> Unit),
    dragEvents: GetLetterDragEvents,
    uiState: LettersRoundViewModelUiState,
    isInteractive: Boolean,
    onClick: (Int) -> Unit
) {
    Row(
        modifier.onGloballyPositioned(onGloballyPositioned)
            .fillMaxWidth().height(50.dp).border(width = 3.dp, color = Color.Green)

    ) {
        uiState.answer.forEach { id ->
            val tile = uiState.initialTiles[id]?.tile ?: return
            LetterTitleComponent(
                modifier = letterModifier,
                onGloballyPositioned = onLetterGloballyPositioned.invoke(id),
                value = tile,
                state = when {
                    !isInteractive -> LetterState.DISABLED
                    uiState.draggingLetter == id -> LetterState.DRAGGING
                    else -> LetterState.IDLE
                },
                dragEvents = dragEvents.toEvents(id),
                onClick = { onClick.invoke(id) }
            )
        }
    }
}

@Composable
fun LetterTitleComponent(
    modifier: Modifier.() -> Modifier,
    onGloballyPositioned: (LayoutCoordinates) -> Unit,
    value: Char,
    state: LetterState,
    dragEvents: LetterDragEvents?,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .onGloballyPositioned(onGloballyPositioned)
            .pointerInput(state, dragEvents) {
                if (dragEvents == null || state == LetterState.DISABLED) return@pointerInput

                detectDragGestures(
                    onDragStart = dragEvents.onDragStart,
                    onDrag = { a, b -> dragEvents.onDrag(a, b, this) },
                    onDragEnd = dragEvents.onDragEnd,
                    onDragCancel = dragEvents.onDragCancel
                )
            }
            .modifier()
            .size(Draggable_Letter_Size).padding(4.dp)
            .alpha(if (state == LetterState.DRAGGING) 0.5f else 1f),
        enabled = state != LetterState.DISABLED,
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        shape = RectangleShape
    ) {
        Text(
            text = value.toString(),
            maxLines = 1
        )
    }
}

private val Draggable_Letter_Size = 40.dp
private val Draggable_Letter_Space = 0.dp

object LettersUi {
    fun calculateDropIndex(density: Density, position: Offset, box: Rect, currentSize: Int): Int {
        with(density) {
            val itemWidth = Draggable_Letter_Size + Draggable_Letter_Space
            val dropIndex = ((position.x - box.left) / itemWidth.toPx()).toInt().coerceIn(0, currentSize)
            return dropIndex
        }
    }
}

@Preview
@Composable
fun LettersTitlesPreview() {
    val lettersViewModel: LettersRoundViewModel = viewModel { LettersRoundViewModel() }

    lettersViewModel.initWithRoundData(
        GameRoundData.Letters(
            listOf('A', 'Z', 'R', 'A', 'Z', 'R', 'A', 'Z', 'R', 'M')
        )
    )

    LettersRoundComponent(
        isInteractive = true,
        listener = object : LettersRoundListener {
            override fun onPlayerAnswerChanged(answer: String) {}
        },
        lettersViewModel
    )
}
