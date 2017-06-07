package com.expedia.bookings.hotel.animation.transition

import android.view.View

abstract class TranslateTransition(protected val view: View, protected val origin: Int, protected val target: Int) {
    abstract fun toTarget(f: Float)
    abstract fun toOrigin(f: Float)
}