package fr.helios.dcdl.game.letters

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.Density

interface LetterDragEvents {
    val onDragStart: (Offset) -> Unit
    val onDrag: (a: PointerInputChange, b: Offset, density: Density) -> Unit
    val onDragEnd: () -> Unit
    val onDragCancel: () -> Unit
}

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
