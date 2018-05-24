package com.expedia.bookings.itin.utils

import com.expedia.bookings.itin.common.ItinExpandedMapViewModel.MapUri

interface IActivityLauncher {
    fun launchActivity(intentable: Intentable, id: String, animationDirection: AnimationDirection = AnimationDirection.SLIDE_RIGHT)
    fun launchActivity(intentable: IntentableWithType, id: String, animationDirection: AnimationDirection = AnimationDirection.SLIDE_RIGHT, itinType: String)
    fun launchExternalMapActivity(data: MapUri)
}

enum class AnimationDirection {
    SLIDE_UP,
    SLIDE_RIGHT
}
