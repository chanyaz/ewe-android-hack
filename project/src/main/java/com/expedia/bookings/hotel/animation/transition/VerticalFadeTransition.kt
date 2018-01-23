package com.expedia.bookings.hotel.animation.transition

import android.view.View

class VerticalFadeTransition(private val view: View, private val origin: Int, private val target: Int) {
    private val translateTransition: VerticalTranslateTransition

    init {
        translateTransition = VerticalTranslateTransition(view, origin, target)
    }

    fun fadeIn(progress: Float) {
        translateTransition.toOrigin(progress)
        view.alpha = progress
    }

    fun fadeOut(progress: Float) {
        translateTransition.toTarget(progress)
        view.alpha = 1 - progress
    }
}
