package com.expedia.bookings.tracking

class AppStartupTimeLogger {

    private val notYetInitialized = -1L
    private var appLaunchedTime: Long = notYetInitialized
    private var appLaunchScreenDisplayed: Long = notYetInitialized
    private var appStartupTime: Long = notYetInitialized

    fun calculateAppStartupTime(): Long {
        appStartupTime = appLaunchScreenDisplayed.minus(appLaunchedTime)
        return appStartupTime
    }

    fun setAppLaunchedTime(time: Long) {
        appLaunchedTime = time
    }

    fun setAppLaunchScreenDisplayed(time: Long) {
        appLaunchScreenDisplayed = time
    }

    fun isComplete(): Boolean {
        return appLaunchedTime != notYetInitialized
    }

    fun clear() {
        setAppLaunchedTime(notYetInitialized)
    }
}