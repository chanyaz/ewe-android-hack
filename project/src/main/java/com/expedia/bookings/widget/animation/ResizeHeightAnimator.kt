package com.expedia.bookings.widget.animation

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import java.util.ArrayList

public class ResizeHeightAnimator(animationDuration: Long) : ValueAnimator(), ValueAnimator.AnimatorUpdateListener {
    override fun onAnimationUpdate(anim: ValueAnimator) {
        for (resizeSpec in resizeSpecList) {
            val newHeight = (resizeSpec.startHeight + (resizeSpec.targetHeight - resizeSpec.startHeight) * (animatedValue as Float)).toInt()
            resizeSpec.view.layoutParams.height = newHeight
            resizeSpec.view.requestLayout()
        }
    }

    private data class ResizeSpec(public val view: View, val startHeight: Int, val targetHeight: Int)

    private var resizeSpecList = ArrayList<ResizeSpec>()

    public fun addViewSpec(view: View, targetHeight: Int, startHeight: Int = view.height) {
        resizeSpecList.add(ResizeSpec(view, startHeight, Math.max(0, targetHeight)))
    }

    init {
        setFloatValues(0f, 1f)
        interpolator = AccelerateDecelerateInterpolator()
        setDuration(animationDuration)
        addUpdateListener(this)
    }
}
