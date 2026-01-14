package fr.helios.dcdl.game.letters

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

// Exemple de composable principal pour le round des lettres
@Composable
fun LettersRoundUI(availableLetters: List<Char>) {
    var available by remember { mutableStateOf(availableLetters.toMutableList()) }
    var selected by remember { mutableStateOf(mutableListOf<Char>()) }

    // États pour gérer le drag-drop
    var draggingLetter by remember { mutableStateOf<Char?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val coroutineScope = rememberCoroutineScope()

    var positionInRootTopRow: Offset by remember { mutableStateOf(Offset.Zero) }

    val lettersPositionInTopRow = remember { mutableStateMapOf<Int, Offset>() }

    Box(
        Modifier.border(width = 3.dp, color = Color.Black)
    ) {

        Column(
            modifier = Modifier.padding(64.dp).align(Alignment.TopStart).border(width = 3.dp, color = Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ligne du haut : lettres disponibles
            Text(
                text = "Lettres disponibles",
                fontSize = 50.sp
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(50.dp)
                    .onGloballyPositioned { coordinates ->
                        println("onGloballyPositioned toprow ${coordinates.positionInParent()}")

                        // global position (local also available)
                        positionInRootTopRow = coordinates.positionInParent()

                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(available.size) { index ->
                    DraggableLetter(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                println("onGloballyPositioned letter root $index ${coordinates.positionInRoot()}")
                                println("onGloballyPositioned letter parent $index ${coordinates.positionInParent()}")
                                println("onGloballyPositioned letter parent2 $index ${(coordinates.parentCoordinates?.positionInParent() ?: Offset.Zero)}")
                                lettersPositionInTopRow[index] = coordinates.positionInRoot()
                                    //coordinates.positionInParent() +
                                           // (coordinates.parentLayoutCoordinates?.positionInParent() ?: Offset.Zero)
                            },
                        letter = available[index],
                        onStartDrag = { letter, offset ->
                            println("onStartDrag UP ${available[index]} $offset $positionInRootTopRow")

                            dragOffset = (lettersPositionInTopRow[index] ?: Offset.Zero) //+
                            //Offset(Draggable_Letter_Size.value, Draggable_Letter_Size.value)  //+ offset
                            draggingLetter = letter
                        },
                        onDrag = { offset ->
                            println("onDrag UP ${available[index]} $offset")

                            dragOffset += offset
                        },
                        onEndDrag = {
                            println("onEndDrag UP ${available[index]}")

                            dragOffset = Offset.Zero
                            // Logique pour drop (gérée dans les drop targets)
                            draggingLetter = null
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Ligne du bas : lettres sélectionnées (réordonnables)
            Text("Votre mot")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .dropTarget { position ->
                        draggingLetter?.let { draggingLetterValue ->
                            // Ajouter la lettre droppée à la position approximée
                            //coroutineScope.launch {
                            val dropIndex = calculateDropIndex(position, selected.size)
                            selected.add(dropIndex, draggingLetterValue)
                            available.remove(draggingLetterValue)
                            draggingLetter = null
                            //}
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selected.size) { index ->
                    DraggableLetter(
                        letter = selected[index],
                        onStartDrag = { letter, offset ->
                            println("onStartDrag DOWN ${available[index]}")

                            draggingLetter = letter
                            selected.remove(letter)  // Retirer temporairement pour drag
                        },
                        onDrag = { offset ->
                            println("onDrag DOWN ${available[index]}")

                            dragOffset = offset
                        },
                        onEndDrag = {
                            println("onEndDrag DOWN ${available[index]}")

                            // Si pas droppé ailleurs, réinsérer à l'origine
                            draggingLetter?.let { draggingLetterValue ->
                                selected.add(index, draggingLetterValue)
                                draggingLetter = null
                            }
                        }
                    )
                }
            }

        }

        var draggingLetterPosition by remember { mutableStateOf(Offset.Zero) }

        // Overlay pour afficher la lettre en drag (si besoin pour feedback visuel)
        draggingLetter?.let { draggingLetter ->
            DraggableLetter(
                modifier = Modifier.align(Alignment.TopStart)
                    .onGloballyPositioned { coordinates ->
                        println("onGloballyPositioned dragging root ${coordinates.positionInRoot()}")
                        println("onGloballyPositioned dragging parent ${coordinates.positionInParent()}")

                        // global position (local also available)
                        draggingLetterPosition = coordinates.positionInRoot() //0.0 if no padding

                    }
                    //.offset { (dragOffset).toIntOffset() }
                    .offset { (dragOffset - draggingLetterPosition).toIntOffset() }
                    //.offset { dragOffset.toIntOffset() }
                    .zIndex(1f),
                letter = draggingLetter,
                onStartDrag = { r, t -> },
                onDrag = {},
                onEndDrag = {}
            )

            /*Text(
                draggingLetter.toString(),
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        println("onGloballyPositioned dragging ${coordinates.positionInParent()}")

                        // global position (local also available)
                        draggingLetterPosition = coordinates.positionInParent()

                    }
                    .offset {  (dragOffset - draggingLetterPosition).toIntOffset() }
                    //.offset { dragOffset.toIntOffset() }
                    .zIndex(1f)
            )*/

        }
    }
}

private val Draggable_Letter_Size = 40.dp
// Composable pour une lettre draggable
@Composable
private fun DraggableLetter(
    modifier: Modifier = Modifier,
    letter: Char,
    onStartDrag: (Char, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onEndDrag: () -> Unit
) {
    Box(
        modifier = modifier
            .border(width = 3.dp, color = Color.Black)
            .size(Draggable_Letter_Size)
            .background(MaterialTheme.colorScheme.primary)
            .dragAndDropSource(
                drawDragDecoration = TODO(),
                transferData = TODO()
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(letter.toString(), color = MaterialTheme.colorScheme.onPrimary)
    }
}

// Modifier personnalisé pour drop target (simulé pour web)
private fun Modifier.dropTarget(onDrop: (Offset) -> Unit): Modifier = pointerInput(Unit) {
    // Pour web, on simule le drop en détectant le end drag près de la zone
    // Plus avancé : utiliser detectTapGestures ou position globale
    // Pour simplicité, on assume que le drop est géré au end drag avec position globale
}

// Fonction utilitaire pour calculer l'index de drop (basé sur position approximée)
private fun calculateDropIndex(position: Offset, currentSize: Int): Int {
    // Logique simple : diviser la position x par largeur de lettre (ex: 48.dp par item)
    val itemWidth = 48f
    val dropIndex = (position.x / itemWidth).toInt().coerceIn(0, currentSize)
    return dropIndex
}

// Extension pour Offset to IntOffset (pour modifier.offset)
private fun Offset.toIntOffset() = IntOffset(x.toInt(), y.toInt())