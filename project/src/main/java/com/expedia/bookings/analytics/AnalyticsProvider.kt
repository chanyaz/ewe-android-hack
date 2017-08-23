package com.expedia.bookings.analytics

import android.app.Activity

interface AnalyticsProvider {
    fun enableDebugLogging(enable: Boolean)

    fun getVisitorId(): String
    fun getUrlWithVisitorData(url: String?): String

    fun trackAction(action: String?, data: Map<String, Any>)
    fun trackState(state: String?, data: Map<String, Any>)

    fun onResumeActivity(activity: Activity)
    fun onPauseActivity()
}