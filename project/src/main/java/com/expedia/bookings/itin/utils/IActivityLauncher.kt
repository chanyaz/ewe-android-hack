package com.expedia.bookings.itin.utils

interface IActivityLauncher {
    fun launchActivity(intentable: Intentable, id: String, animationDirection: AnimationDirection = AnimationDirection.SLIDE_RIGHT)
}

enum class AnimationDirection {
    SLIDE_UP,
    SLIDE_RIGHT
}
