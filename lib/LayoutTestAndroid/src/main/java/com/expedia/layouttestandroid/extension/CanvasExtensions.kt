package com.expedia.layouttestandroid.extension

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff

fun Canvas.clearCanvas() {
    drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC)
}