package com.expedia.layouttestandroid.helper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.os.Build
import com.expedia.layouttestandroid.extension.clearCanvas

object BitmapProvider {
    private val cachedBitmapAndCanvas = HashMap<Point, BitmapAndCanvas>()

    fun getBitmap(width: Int, height: Int): BitmapAndCanvas {
        cachedBitmapAndCanvas[Point(width, height)]?.let {
            val bitmapAndCanvas = it

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                bitmapAndCanvas.bitmap.reconfigure(width, height, Bitmap.Config.ARGB_4444)
                bitmapAndCanvas.canvas = Canvas(bitmapAndCanvas.bitmap)
            }
            bitmapAndCanvas.canvas.clearCanvas()
            return bitmapAndCanvas
        }

        val bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bitmap)
        val bitmapAndCanvas = BitmapAndCanvas(bitmap, canvas)

        cachedBitmapAndCanvas[Point(width, height)] = bitmapAndCanvas
        return bitmapAndCanvas
    }
}