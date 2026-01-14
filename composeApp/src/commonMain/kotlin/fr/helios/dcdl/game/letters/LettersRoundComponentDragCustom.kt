package fr.helios.dcdl.game.letters

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun LettersRoundUICustom(availableLetters: List<Char>) {
    var available = remember { mutableStateListOf(*availableLetters.toTypedArray()) }
    var selected by remember { mutableStateOf(listOf<Char>()) }

    var draggingLetter by remember { mutableStateOf<Char?>(null) }
    var draggingLetterOffset by remember { mutableStateOf(Offset.Zero) }
    var cursorOffsetInDraggingLetter by remember { mutableStateOf(Offset.Zero) }

    val availablePositionRoot = remember { mutableStateMapOf<Int, Offset>() }
    //val selectedPositionRoot = remember { mutableStateMapOf<Int, Offset>() }

    var availableRowBounds by remember { mutableStateOf<Rect>(Rect.Zero) }
    var selectedRowBounds by remember { mutableStateOf<Rect>(Rect.Zero) }

    Box(
        Modifier.border(width = 3.dp, color = Color.Black)
    ) {

        Column(
            modifier = Modifier.padding(64.dp).align(Alignment.TopStart).border(width = 3.dp, color = Color.Black)
                /*.pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            println("onStartDrag COLUMN")
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            println("onDrag COLUMN")
                        },
                        onDragEnd = {
                            println("onDragEnd COLUMN")
                        },
                        onDragCancel = {
                            println("onDragCancel COLUMN")
                        }
                    )
                }*/,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tirage : ",
                fontSize = 50.sp
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth().height(50.dp)
                    .onGloballyPositioned { coordinates ->
                        println("onGloballyPositioned UP ${coordinates.boundsInRoot()}")
                        availableRowBounds = coordinates.boundsInRoot()
                    },
                horizontalArrangement = Arrangement.spacedBy(Draggable_Letter_Space)
            ) {
                items(available.size) { index ->
                    DraggableLetter(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                availablePositionRoot[index] = coordinates.positionInRoot()
                            },
                        letter = available[index],
                        onStartDrag = { letter, offset ->
                            println("onStartDrag UP ${available[index]}")

                            cursorOffsetInDraggingLetter = offset

                            draggingLetter = letter
                            draggingLetterOffset = availablePositionRoot[index] ?: Offset.Zero
                        },
                        onDrag = { offset ->
                            println("onDrag UP ${available[index]} $offset")

                            draggingLetterOffset += offset

                            draggingLetter?.let { draggingLetter ->
                                val cursorOffset = draggingLetterOffset + cursorOffsetInDraggingLetter
                                if (selectedRowBounds.contains(cursorOffset)) {
                                    println(selected)

                                    val hoveredIndex = calculateDropIndex(
                                        cursorOffset,
                                        selectedRowBounds,
                                        selected.size
                                    )


                                    val newSelected = selected.toMutableList()

                                    if (selected.contains(draggingLetter)) {
                                        val draggingLetterIndex = selected.indexOf(draggingLetter)
                                        println("hoveredIndex $hoveredIndex")
                                        println("draggingLetterIndex $draggingLetterIndex")

                                        if (draggingLetterIndex != hoveredIndex) {
                                            newSelected.removeAt(draggingLetterIndex)

                                            val indexToAdd = if (draggingLetterIndex < hoveredIndex) {
                                                hoveredIndex - 1
                                            } else {
                                                hoveredIndex
                                            }
                                            println("index to add $indexToAdd")

                                            newSelected.add(indexToAdd, draggingLetter)

                                        }
                                    } else {
                                        newSelected.add(hoveredIndex, draggingLetter)
                                    }

                                    selected = newSelected




                                    /*println(selectedPositionRoot.toList())

                                    val hoveredLetter = selectedPositionRoot.filter { it.value.x < cursorOffset.x }
                                        .maxOfOrNull { it.key }
                                    val hoveredIndex = hoveredLetter ?: 0

                                    if(hoveredIndex != selected.indexOf(draggingLetter)) {
                                        selected.remove(draggingLetter)
                                    }

                                    if (!selected.contains(draggingLetter)) {
                                        val indexToAdd = selectedPositionRoot.filter { it.value.x < cursorOffset.x }
                                            .maxOfOrNull { it.key } ?: 0

                                        selected.add(indexToAdd, draggingLetter)

                                        println("HOVER: $indexToAdd / current ${selected.indexOf(draggingLetter)}")

                                    }*/


                                    //val indexToAdd = hoveredLetter?.let { it.key + 1 } ?: 0

                                    println(selected)
                                } else {
                                    //selected.remove(draggingLetter)
                                }
                            }
                        },
                        onEndDrag = {
                            println("onEndDrag UP ${available[index]} $draggingLetterOffset $cursorOffsetInDraggingLetter")

                            draggingLetter?.let { draggingLetter ->
                                println("down contains ${selectedRowBounds.contains(draggingLetterOffset)}")
                                println("down contains ${selectedRowBounds.contains(draggingLetterOffset + cursorOffsetInDraggingLetter)}")
                                if (selectedRowBounds.contains(draggingLetterOffset + cursorOffsetInDraggingLetter)) {
                                    println(selected)
                                    //selected.add(draggingLetter)
                                    println(selected)
                                }
                            }

                            draggingLetter = null
                            draggingLetterOffset = Offset.Zero
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("Proposition :")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp).border(width = 3.dp, color = Color.Green)
                    .padding(horizontal = Draggable_Letter_Space / 2)
                    .onGloballyPositioned { coordinates ->
                        println("onGloballyPositioned DOWN ${coordinates.boundsInRoot()}")
                        selectedRowBounds = coordinates.boundsInRoot()
                    }
                    /*.dropTarget { position ->
                        draggingLetter?.let { draggingLetterValue ->
                            val dropIndex = calculateDropIndex(position, selected.size)
                            selected.add(dropIndex, draggingLetterValue)
                            available.remove(draggingLetterValue)
                            draggingLetter = null
                        }
                    }*/,
                horizontalArrangement = Arrangement.spacedBy(Draggable_Letter_Space)
            ) {
                (selected.size until available.size).forEach {
                    //selectedPositionRoot.remove(it)
                }
                items(selected.size) { index ->
                    DraggableLetter(
                        Modifier.onGloballyPositioned { coordinates ->
                            //selectedPositionRoot[index] = coordinates.positionInRoot()
                        },
                        letter = selected[index],
                        onStartDrag = { letter, offset ->
                            println("onStartDrag DOWN ${available[index]}")

                            draggingLetter = letter
                            //selected.remove(letter)
                        },
                        onDrag = { offset ->
                            println("onDrag DOWN ${available[index]}")

                            draggingLetterOffset = offset
                        },
                        onEndDrag = {
                            println("onEndDrag DOWN ${available[index]}")

                            // Si pas droppé ailleurs, réinsérer à l'origine
                            draggingLetter?.let { draggingLetterValue ->
                                //selected.add(index, draggingLetterValue)
                                draggingLetter = null
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

        }

        var draggingLetterPosition by remember { mutableStateOf(Offset.Zero) }

        draggingLetter?.let { draggingLetter ->
            DraggableLetter(
                modifier = Modifier.align(Alignment.TopStart)
                    .onGloballyPositioned { coordinates ->
                        draggingLetterPosition = coordinates.positionInRoot()
                    }
                    .offset { (draggingLetterOffset - draggingLetterPosition).toIntOffset() }
                    .zIndex(1f),
                letter = draggingLetter
            )
        }
    }
}

private val Draggable_Letter_Size = 40.dp
private val Draggable_Letter_Space = 8.dp

@Composable
private fun DraggableLetter(
    modifier: Modifier = Modifier,
    letter: Char,
    onStartDrag: (Char, Offset) -> Unit = { _, _ -> },
    onDrag: (Offset) -> Unit = {},
    onEndDrag: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .border(width = 3.dp, color = Color.Black)
            .size(Draggable_Letter_Size)
            .background(MaterialTheme.colorScheme.primary)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onStartDrag(letter, offset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = onEndDrag,
                    onDragCancel = onEndDrag
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(letter.toString(), color = MaterialTheme.colorScheme.onPrimary)
    }
}



// Modifier personnalisé pour drop target (simulé pour web)
private fun Modifier.dropTarget(onDrop: (Offset) -> Unit): Modifier = this.pointerInput(Unit) {
    // Pour web, on simule le drop en détectant le end drag près de la zone
    // Plus avancé : utiliser detectTapGestures ou position globale
    // Pour simplicité, on assume que le drop est géré au end drag avec position globale

    detectDragGestures(
        onDragCancel = {
            println("DROP-TARGET onDragCancel")
            onDrop.invoke(Offset.Zero)
        },
        onDragEnd = {
            println("DROP-TARGET onDragEnd")
            onDrop.invoke(Offset.Zero)
        },
        onDragStart = {},
        onDrag = { _, _ -> }
    )
}

private fun calculateDropIndex(position: Offset, box: Rect, currentSize: Int): Int {
    val itemWidth = Draggable_Letter_Size + Draggable_Letter_Space
    val dropIndex = ((position.x - box.left) / itemWidth.value).toInt().coerceIn(0, currentSize)
    return dropIndex
}

private fun Offset.toIntOffset() = IntOffset(x.toInt(), y.toInt())