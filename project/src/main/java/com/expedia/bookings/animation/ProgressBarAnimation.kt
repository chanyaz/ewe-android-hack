package com.expedia.bookings.animation

import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar

class ProgressBarAnimation(var progressBar: ProgressBar, var from: Float, var to: Float) : Animation() {

    override fun applyTransformation(interpolatedTime: Float, transformation: Transformation?) {
        super.applyTransformation(interpolatedTime, transformation)
        val value = from + (to - from) * interpolatedTime
        progressBar.progress = value.toInt()
    }
}
