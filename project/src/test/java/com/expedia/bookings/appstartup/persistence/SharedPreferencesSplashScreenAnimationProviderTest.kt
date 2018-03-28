package com.expedia.bookings.appstartup.persistence

class MockSharedPreferencesSplashScreenAnimationProvider : SplashScreenAnimationProvider {
    private var shouldSplashAnimationRun: Boolean = false

    override fun shouldSplashAnimationRun(): Boolean {
        return shouldSplashAnimationRun
    }

    override fun put(shouldSplashAnimationRun: Boolean) {
        this.shouldSplashAnimationRun = shouldSplashAnimationRun
    }
}
