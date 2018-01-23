package com.expedia.bookings.analytics

import android.app.Activity
import com.adobe.mobile.Analytics
import com.adobe.mobile.Config
import com.adobe.mobile.Visitor

class OmnitureAnalyticsProvider : AnalyticsProvider {
    override fun enableDebugLogging(enable: Boolean) {
        Config.setDebugLogging(enable)
    }

    override fun getVisitorId(): String = Visitor.getMarketingCloudId()

    override fun getUrlWithVisitorData(url: String?): String = Visitor.appendToURL(url)

    override fun trackAction(action: String?, data: Map<String, Any>) {
        Analytics.trackAction(action, data)
    }

    override fun trackState(state: String?, data: Map<String, Any>) {
        Analytics.trackState(state, data)
    }

    override fun onResumeActivity(activity: Activity) {
        Config.collectLifecycleData(activity)
    }

    override fun onPauseActivity() {
        Config.pauseCollectingLifecycleData()
    }
}
