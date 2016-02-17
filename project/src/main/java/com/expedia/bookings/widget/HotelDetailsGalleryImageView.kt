package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui

public class HotelDetailsGalleryImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {

    private var actualHeight = context.resources.getDimensionPixelSize(R.dimen.car_details_image_size)

    override fun setImageBitmap(bitmap: Bitmap?) {
        super.setImageBitmap(bitmap)
        if (bitmap != null) {
            actualHeight = (bitmap.height * width ) / bitmap.width;
        }
    }

    fun setIntermediateValue(startHeight: Int, finalHeight: Int, value: Float) {
        val normValue = normalizeValue(1 - value)
        val height = (startHeight + (actualHeight - startHeight) * normValue).toInt()

        layoutParams = FrameLayout.LayoutParams(Ui.getScreenSize(getContext()).x, height)

        var actualScrollHeight = (finalHeight - startHeight) * (1 - normValue)
        (parent as View).translationY = actualScrollHeight
        (parent as View).translationY += (finalHeight - actualScrollHeight - layoutParams.height) / 2
    }

    private fun normalizeValue(value: Float): Float {
        if (value < 0)
            return 0f
        if (value > 1f)
            return 1f
        return value
    }
}