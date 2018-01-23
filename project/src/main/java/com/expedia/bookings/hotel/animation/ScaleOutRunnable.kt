package com.expedia.bookings.hotel.animation

import android.view.View

class ScaleOutRunnable(view: View, duration: Long, startDelay: Long) : ScaleRunnable(view, duration, startDelay) {
    override fun startScale(): Float {
        return 1f
    }

    override fun endScale(): Float {
        return 0f
    }

    override fun getYPivot(): Float {
        return 0f
    }

    init {
        endSubject.subscribe {
            viewRef.get()?.visibility = View.GONE
        }
    }
}
