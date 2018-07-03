package com.expedia.layouttestandroid.extension

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.createScaledBitmap(src: Bitmap, dstWidth: Int, dstHeight: Int, filter: Boolean): Bitmap {
    val m = Matrix()

    val width = src.width
    val height = src.height
    if (width != dstWidth || height != dstHeight) {
        val sx = dstWidth / width.toFloat()
        val sy = dstHeight / height.toFloat()
        m.setScale(sx, sy)
    }
    return Bitmap.createBitmap(src, 0, 0, width, height, m, filter)
}
