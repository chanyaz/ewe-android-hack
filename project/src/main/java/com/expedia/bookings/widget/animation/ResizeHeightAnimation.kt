package com.expedia.bookings.widget.animation

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import java.util.ArrayList

public class ResizeHeightAnimation() : Animation() {
    private data class ResizeSpec(public val view: View, val startHeight: Int, val targetHeight: Int)

    private var resizeSpecList: MutableList<ResizeSpec> = ArrayList()

    public fun addViewSpec(view: View, targetHeight: Int) {
        resizeSpecList.add(ResizeSpec(view, view.height, if (targetHeight < 0) 0 else targetHeight))
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        for (resizeSpec in resizeSpecList) {
            val newHeight = (resizeSpec.startHeight + (resizeSpec.targetHeight - resizeSpec.startHeight) * interpolatedTime).toInt()
            resizeSpec.view.layoutParams.height = newHeight
            resizeSpec.view.requestLayout()
        }
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}


