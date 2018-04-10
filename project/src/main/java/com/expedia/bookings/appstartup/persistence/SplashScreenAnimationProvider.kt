package com.expedia.bookings.appstartup.persistence

interface SplashScreenAnimationProvider {
    fun shouldSplashAnimationRun(): Boolean
    fun put(shouldSplashAnimationRun: Boolean)
}
