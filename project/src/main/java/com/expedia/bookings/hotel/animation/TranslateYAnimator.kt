package com.expedia.bookings.hotel.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View

class TranslateYAnimator(
    view: View,
    startY: Float,
    endY: Float,
    duration: Long? = null,
    private val startAction: () -> Unit = {},
    private val endAction: () -> Unit = {}
) {
    private val translateAnimator = ObjectAnimator.ofFloat(view, "translationY", startY, endY)

    init {
        duration?.let { translateAnimator.duration = it }
        translateAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                startAction()
            }

            override fun onAnimationEnd(animation: Animator?) {
                endAction()
            }
        })
    }

    fun start() {
        translateAnimator.start()
    }
}
