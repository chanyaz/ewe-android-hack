package com.expedia.bookings.analytics

import android.app.Activity

open class LoggingAnalyticsProvider : AnalyticsProvider {
    override fun enableDebugLogging(enable: Boolean) {
    }

    override fun getVisitorId(): String {
        return "testVisitorId"
    }

    override fun getUrlWithVisitorData(url: String?): String {
        return "testUrlWithVisitorData"
    }

    override fun trackAction(action: String?, data: Map<String, Any>) {
        printDataMap(data)
    }

    override fun trackState(state: String?, data: Map<String, Any>) {
        printDataMap(data)
    }

    override fun onResumeActivity(activity: Activity) {
    }

    override fun onPauseActivity() {
    }

    private fun printDataMap(data: Map<String, Any>) {
        data.forEach { key, value ->
            println("$key : $value")
        }
    }
}
