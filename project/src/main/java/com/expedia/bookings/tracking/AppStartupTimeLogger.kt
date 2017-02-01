package com.expedia.bookings.tracking

import com.expedia.bookings.utils.ClientLogConstants
import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.services.ClientLogServices

class AppStartupTimeLogger {

    private var appLaunched: Long = -1L
    private var appLaunchScreenDisplayed: Long = -1L
    private var appStartupTime: Long = -1L

    fun calculateAppStartupTime(): Long {
        appStartupTime = appLaunchScreenDisplayed.minus(appLaunched)
        return appStartupTime
    }

    fun setAppLaunchedTime(time: Long) {
        appLaunched = time
    }

    fun getAppLaunchedTime(): Long {
        return appLaunched
    }

    fun setAppLaunchScreenDisplayed(time: Long) {
        appLaunchScreenDisplayed = time
    }

    fun isComplete(): Boolean {
        return getAppLaunchedTime() != -1L
    }

    fun clear() {
        setAppLaunchedTime(-1L)
    }
}