package fr.helios.dcdl.game.letters

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputEventHandler
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.helios.dcdl.model.GameRoundData
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

    var cursorOffsetInDraggingLetter by remember { mutableStateOf(Offset.Zero) }

    val availablePositionRoot = remember { mutableStateMapOf<Int, Offset>() }
    val selectedPositionRoot = remember { mutableStateMapOf<Int, Offset>() }

    var selectedRowBounds by remember { mutableStateOf(Rect.Zero) }

    fun onLetterDragStart(tileId: Int, getLetterOffset: (Int) -> Offset) : (Offset) -> Unit = { cursorOffset ->
        cursorOffsetInDraggingLetter = cursorOffset
        lettersViewModel.updateDraggingLetter(
            draggingLetterOffset = getLetterOffset(tileId),
            draggingLetter = tileId
        )
    }

    fun onLetterDrag(): (PointerInputChange, Offset, density: Density) -> Unit = { change, offset, density -> //todo in VM
        change.consume()
        lettersUiState.draggingLetter?.let { draggingLetter ->

            lettersViewModel.updateDraggingLetter(
                draggingLetterOffset = lettersUiState.draggingLetterOffset + offset
            )

            val cursorOffset = lettersUiState.draggingLetterOffset + cursorOffsetInDraggingLetter
            if (selectedRowBounds.contains(cursorOffset)) {
                val indexToAdd = density.calculateDropIndex(
                    cursorOffset,
                    selectedRowBounds,
                    lettersUiState.answer.size
                )

                lettersViewModel.updateAnswerTiles(
                    indexToInsert = indexToAdd,
                    tileId = draggingLetter
                )
            } else {
                lettersViewModel.removeAnswerTile(
                    tileId = draggingLetter
                )
            }
        }
    }

    fun letterDragGesture(tileId: Int?, letterOffset: (Int) -> Offset?) = PointerInputEventHandler {
        detectDragGestures(
            onDragStart = { offset ->
                if (tileId == null) return@detectDragGestures
                cursorOffsetInDraggingLetter = offset
                lettersViewModel.updateDraggingLetter(
                    draggingLetterOffset = letterOffset(tileId) ?: Offset.Zero,
                    draggingLetter = tileId
                )
            },
            onDrag = { change, offset ->
                change.consume()
                lettersUiState.draggingLetter?.let { draggingLetter ->

                    lettersViewModel.updateDraggingLetter(
                        draggingLetterOffset = lettersUiState.draggingLetterOffset + offset
                    )

                    val cursorOffset = lettersUiState.draggingLetterOffset + cursorOffsetInDraggingLetter
                    if (selectedRowBounds.contains(cursorOffset)) {
                        val indexToAdd = calculateDropIndex(
                            cursorOffset,
                            selectedRowBounds,
                            lettersUiState.answer.size
                        )

                        lettersViewModel.updateAnswerTiles(
                            indexToInsert = indexToAdd,
                            tileId = draggingLetter
                        )
                    } else {
                        lettersViewModel.removeAnswerTile(
                            tileId = draggingLetter
                        )
                    }
                }
            }
        )
    }

    Column(modifier =
        Modifier.//pointerInput(Unit, letterDragGesture(null) { null })
        pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {},
                onDrag = { a, b -> onLetterDrag().invoke(a, b, this) },//onLetterDrag(),
                onDragEnd = { lettersViewModel.updateDraggingLetter(draggingLetter = null) },
                onDragCancel = { lettersViewModel.updateDraggingLetter(draggingLetter = null) },
            )
        }
    ) {

        LetterInitialTilesComponent(
            letterModifier = { tile ->
                Modifier
                    .onGloballyPositioned { coordinates ->
                        availablePositionRoot[tile] = coordinates.positionInRoot()
                    }
                    //.pointerInput(Unit, letterDragGesture(tile) { availablePositionRoot[it] })
            },

            dragEvents = object: GetLetterDragEvents {
                override fun getOnDragStart(tileId: Int) =
                    onLetterDragStart(tileId) { tileId: Int -> availablePositionRoot[tileId] ?: Offset.Zero }
                override fun getOnDrag() = onLetterDrag()
                override fun getOnDragEnd() = { lettersViewModel.updateDraggingLetter(draggingLetter = null) }
                override fun getOnDragCancel() = { lettersViewModel.updateDraggingLetter(draggingLetter = null) }
            },
            uiState = lettersUiState,
            isInteractive = isInteractive,
            onClick = { lettersViewModel.onClickInitialTile(it) }
        )

        Spacer(Modifier.height(8.dp))

        LetterAnswerTilesComponent(
            letterModifier = { tile ->
                Modifier
                    .onGloballyPositioned { coordinates ->
                        selectedPositionRoot[tile] = coordinates.positionInRoot()
                    }
                    //.pointerInput(Unit, letterDragGesture(tile) { selectedPositionRoot[it] })
            },
            modifier = Modifier.onGloballyPositioned { coordinates ->
                selectedRowBounds = coordinates.boundsInRoot()
            },
            dragEvents = object: GetLetterDragEvents {
                override fun getOnDragStart(tileId: Int) = onLetterDragStart(tileId) { tileId -> selectedPositionRoot[tileId] ?: Offset.Zero }

                override fun getOnDrag() = onLetterDrag()
                override fun getOnDragEnd() = { lettersViewModel.updateDraggingLetter(draggingLetter = null) }
                override fun getOnDragCancel() = { } //so the column keep the track of the drag when the letter is remove from the answer
            },
            uiState = lettersUiState,
            isInteractive = isInteractive
        ) { lettersViewModel.onClickAnswerTile(it) }


        var draggingLetterPosition by remember { mutableStateOf(Offset.Zero) }

        lettersUiState.initialTiles[lettersUiState.draggingLetter]?.let { draggingLetter ->
            LetterTitleComponent(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        draggingLetterPosition = coordinates.positionInRoot()
                    }
                    .offset { (lettersUiState.draggingLetterOffset - draggingLetterPosition).toIntOffset() }
                    .zIndex(1f),
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
    letterModifier: (Int) -> Modifier = { Modifier },
    dragEvents: GetLetterDragEvents,
    uiState: LettersRoundViewModelUiState,
    isInteractive: Boolean,
    onClick: (Int) -> Unit
) {
    LazyRow(
        modifier.fillMaxWidth().height(50.dp).border(width = 3.dp, color = Color.Green)
    ) {
        items(uiState.initialTiles.values.sortedBy { it.id }, key = { it }) { tile ->
            LetterTitleComponent(
                modifier = letterModifier.invoke(tile.id),
                value = tile.tile,
                state = when { //todo: method getState?
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
    letterModifier: (Int) -> Modifier = { Modifier },
    dragEvents: GetLetterDragEvents,
    uiState: LettersRoundViewModelUiState,
    isInteractive: Boolean,
    onClick: (Int) -> Unit
) {
    LazyRow(
        modifier.fillMaxWidth().height(50.dp).border(width = 3.dp, color = Color.Green)
    ) {
        items(uiState.answer, key = { it }) { id ->
            val tile = uiState.initialTiles[id]?.tile ?: return@items
            LetterTitleComponent(
                modifier = letterModifier.invoke(id),
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
    modifier: Modifier,
    value: Char,
    state: LetterState,
    dragEvents: LetterDragEvents?,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .pointerInput(Unit) {
                if (dragEvents == null) return@pointerInput
                detectDragGestures(
                    onDragStart = dragEvents.onDragStart,
                    onDrag = { a, b -> dragEvents.onDrag(a, b, this) },
                    onDragEnd = dragEvents.onDragEnd,
                    onDragCancel = dragEvents.onDragCancel
                )
            }.size(Draggable_Letter_Size).padding(4.dp)
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

fun Density.calculateDropIndex(position: Offset, box: Rect, currentSize: Int): Int {
    val itemWidth = Draggable_Letter_Size + Draggable_Letter_Space
    val dropIndex = ((position.x - box.left) / itemWidth.toPx()).toInt().coerceIn(0, currentSize)
    return dropIndex
}

private fun Offset.toIntOffset() = IntOffset(x.toInt(), y.toInt())

interface GetLetterDragEvents {
    fun getOnDragStart(tileId: Int): (Offset) -> Unit
    fun getOnDrag(): (PointerInputChange, Offset, density: Density) -> Unit
    fun getOnDragEnd(): () -> Unit
    fun getOnDragCancel(): () -> Unit

    fun toEvents(tileId: Int) = object: LetterDragEvents {
        override val onDrag: (a: PointerInputChange, b: Offset, density: Density) -> Unit = getOnDrag()
        override val onDragStart = getOnDragStart(tileId)
        override val onDragEnd = getOnDragEnd()
        override val onDragCancel = getOnDragCancel()
    }
}

interface LetterDragEvents {
    val onDragStart: (Offset) -> Unit
    val onDrag: (a: PointerInputChange, b:Offset, density: Density) -> Unit
    //fun onDrag(density: Density): (a: PointerInputChange, b:Offset) -> Unit
    val onDragEnd: () -> Unit
    val onDragCancel: () -> Unit
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