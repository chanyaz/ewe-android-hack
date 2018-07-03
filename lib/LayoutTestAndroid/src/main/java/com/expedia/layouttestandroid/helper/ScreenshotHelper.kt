package com.expedia.layouttestandroid.helper

import android.graphics.Bitmap
import android.graphics.Rect
import android.view.View
import com.expedia.layouttestandroid.Constants.Companion.SCALED_OUTPUT_IMAGE_SIZE

object ScreenshotHelper {

    fun capture(measuredView: View): Bitmap {
        if (measuredView.width == 0 || measuredView.height == 0) {
            throw RuntimeException("View width or/and height is not 0 - width: ${measuredView.width} height: ${measuredView.height}")
        }

        val (bitmap, canvas) = BitmapProvider.getBitmap(measuredView.width, measuredView.height)
        measuredView.draw(canvas)
        return bitmap
    }

    fun getScaledBitmap(bitmap: Bitmap, scaledWidthOrHeight: Float = SCALED_OUTPUT_IMAGE_SIZE): Bitmap {
        val (width, height) = computerWidthAndHeight(bitmap.width.toFloat(), bitmap.height.toFloat(), scaledWidthOrHeight)

        val dstRectForRender = Rect(0, 0, width.toInt(), height.toInt())
        val bitmapAndCanvas = BitmapProvider.getBitmap(width.toInt(), height.toInt())
        bitmapAndCanvas.canvas.drawBitmap(bitmap, null, dstRectForRender, null)
        return bitmap
    }

    private fun computerWidthAndHeight(width: Float, height: Float, scaledWidthOrHeight: Float): Pair<Float, Float> {
        if (width <= scaledWidthOrHeight || height <= scaledWidthOrHeight) {
            return Pair(width, height)
        }
        if (width < height) {
            return Pair(scaledWidthOrHeight, scaledWidthOrHeight * (height / width))
        }
        return Pair(scaledWidthOrHeight * (width / height), scaledWidthOrHeight)
    }
}