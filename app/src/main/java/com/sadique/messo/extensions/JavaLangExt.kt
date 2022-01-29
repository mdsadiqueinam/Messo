package com.sadique.messo.extensions

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

@ColorInt
fun lightenColor(
    @ColorInt color: Int?,
    value: Float,
): Int? {
    if (color == null) return null
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color, hsl)
    hsl[2] += value
    hsl[2] = 0f.coerceAtLeast(hsl[2].coerceAtMost(1f))
    return ColorUtils.HSLToColor(hsl)
}