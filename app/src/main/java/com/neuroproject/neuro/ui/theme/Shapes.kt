package com.neuroproject.neuro.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(15.dp),
    large = RoundedCornerShape(40.dp),
    extraLarge = RoundedCornerShape(56.dp)
)

/**
 * Константы скругления углов для элементов интерфейса.
 */
object AppRadii {
    val button = 40.dp
    val chip = 15.dp
}