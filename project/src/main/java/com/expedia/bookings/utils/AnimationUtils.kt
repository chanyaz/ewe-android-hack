package com.expedia.bookings.utils

import android.animation.ValueAnimator
import android.view.View

object AnimationUtils {

    @JvmStatic fun animateView(view: View, fromHeight: Int, toHeight: Int, animDuration: Long?, startDelay: Long?): ValueAnimator {
        val anim = ValueAnimator.ofInt(fromHeight, toHeight)
        anim.duration = animDuration!!
        anim.startDelay = startDelay!!
        anim.addUpdateListener { valueAnimator -> view.scrollY = valueAnimator.animatedValue as Int }
        return anim
    }
}
