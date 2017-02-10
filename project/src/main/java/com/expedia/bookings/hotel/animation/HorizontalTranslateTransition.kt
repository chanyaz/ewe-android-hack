package com.expedia.bookings.hotel.animation

import android.view.View

class HorizontalTranslateTransition(view: View, origin: Int, target: Int) : TranslateTransition(view, origin, target) {

    override fun toOrigin(progress: Float) {
        view.translationX = target + (progress * (origin - target))
    }

    override fun toTarget(progress: Float) {
        view.translationX = origin + (progress * (target - origin))
    }

    fun toOrigin() {
        view.translationX = origin.toFloat()
    }

    fun toTarget() {
        view.translationX = target.toFloat()
    }
}
