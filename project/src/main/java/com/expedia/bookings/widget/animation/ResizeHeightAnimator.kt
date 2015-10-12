package com.expedia.bookings.widget.animation

import android.animation.ValueAnimator
import android.view.View
import java.util.ArrayList

public class ResizeHeightAnimator(val animationDuration: Long) : ValueAnimator(), ValueAnimator.AnimatorUpdateListener {
    override fun onAnimationUpdate(p0: ValueAnimator?) {
        for (resizeSpec in resizeSpecList) {
            val newHeight = (resizeSpec.startHeight + (resizeSpec.targetHeight - resizeSpec.startHeight) * (animatedValue as Float)).toInt()
            resizeSpec.view.layoutParams.height = newHeight
            resizeSpec.view.requestLayout()
        }
    }

    private data class ResizeSpec(public val view: View, val startHeight: Int, val targetHeight: Int)

    private var resizeSpecList: MutableList<ResizeSpec> = ArrayList()

    public fun addViewSpec(view: View, targetHeight: Int) {
        resizeSpecList.add(ResizeSpec(view, view.height, if (targetHeight < 0) 0 else targetHeight))
    }

    init {
        setFloatValues(0f, 1f)
        setDuration(animationDuration)
        addUpdateListener(this)
    }
}
