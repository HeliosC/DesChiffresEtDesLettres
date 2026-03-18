package fr.helios.dcdl.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset

object OffsetExt {
    fun Offset.toIntOffset() = IntOffset(x.toInt(), y.toInt())
}
