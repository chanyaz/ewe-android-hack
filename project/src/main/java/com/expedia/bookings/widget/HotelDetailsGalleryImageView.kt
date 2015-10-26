package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import com.expedia.bookings.utils.Ui

public class HotelDetailsGalleryImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {

    private var isLandscapeImage = false
    private var actualHeight = 0

    override fun setImageBitmap(bitmap: Bitmap?) {
        super.setImageBitmap(bitmap)
        if (bitmap != null) {
            isLandscapeImage = bitmap.width > bitmap.height
            actualHeight = (bitmap.height * width ) / bitmap.width;
        }
    }

    fun setIntermediateValue(startHeight: Int, finalHeight: Int, value: Float) {
        if (actualHeight != 0) {
            val normValue = normalizeValue(1 - value)
            setScaleType(ImageView.ScaleType.CENTER_CROP)

            val layoutParams = getLayoutParams()
            layoutParams.height = getCalculatedHeight(startHeight, normValue)
            setLayoutParams(layoutParams)

            var actualScrollHeight = (finalHeight - startHeight) * (1 - normValue)
            translationY = actualScrollHeight
            translationY += (finalHeight - actualScrollHeight - layoutParams.height) / 2
        }
    }

    private fun getCalculatedHeight(minHeight: Int, value: Float): Int {
        if (isLandscapeImage)
            return (minHeight + (actualHeight - minHeight) * value).toInt()
        else
            return (actualHeight + (minHeight - actualHeight) * (1 - value)).toInt()
    }

    private fun normalizeValue(value: Float): Float {
        if (value < 0)
            return 0f
        if (value > 1f)
            return 1f
        return value
    }
}