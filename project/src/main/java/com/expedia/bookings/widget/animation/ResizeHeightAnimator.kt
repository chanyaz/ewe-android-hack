package com.expedia.bookings.widget.animation

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import java.util.ArrayList

class ResizeHeightAnimator(animationDuration: Long) : ValueAnimator(), ValueAnimator.AnimatorUpdateListener {
    override fun onAnimationUpdate(anim: ValueAnimator) {
        for (resizeSpec in resizeSpecList) {
            val newHeight = (resizeSpec.startHeight + (resizeSpec.targetHeight - resizeSpec.startHeight) * (animatedValue as Float)).toInt()
            val layoutParams = resizeSpec.view.layoutParams
            layoutParams.height = newHeight
            resizeSpec.view.layoutParams = layoutParams
        }
    }

    private data class ResizeSpec(val view: View, val startHeight: Int, val targetHeight: Int)

    private var resizeSpecList = ArrayList<ResizeSpec>()

    fun addViewSpec(view: View, targetHeight: Int, startHeight: Int = view.height) {
        resizeSpecList.add(ResizeSpec(view, startHeight, Math.max(0, targetHeight)))
    }

    init {
        setFloatValues(0f, 1f)
        interpolator = AccelerateDecelerateInterpolator()
        duration = animationDuration
        addUpdateListener(this)
    }
}
