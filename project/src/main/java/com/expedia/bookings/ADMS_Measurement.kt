package com.expedia.bookings

import android.app.Activity
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureAnalyticsProvider

open class ADMS_Measurement {

    var appState: String? = null

    val visitorID by lazy {
        analyticsProvider.getVisitorId()
    }

    private val cData = HashMap<String, Any>()

    fun setDebugLogging(enable: Boolean) {
        analyticsProvider.enableDebugLogging(enable)
    }

    fun setEvar(i: Int, s: String?) {
        cData[EVAR + i] = s ?: ""
    }

    fun getEvar(i: Int): String? {
        return getOmnitureDataValue(EVAR + i) as String?
    }

    fun setProp(i: Int, s: String?) {
        cData[PROP + i] = s ?: ""
    }

    fun getProp(i: Int): String? {
        return getOmnitureDataValue(PROP + i) as String?
    }

    @Deprecated("Please use the method appendEvents", replaceWith = ReplaceWith("appendEvents(s)"))
    fun setEvents(s: String?) {
        cData[EVENTS] = s ?: ""
    }

    fun appendEvents(eventString: String) {
        val sb = StringBuilder()
        if (getEvents() != null) {
            sb.append(getEvents())
        }

        if (sb.isNotEmpty() && eventString.isNotEmpty()) {
            sb.append(",")
        }
        sb.append(eventString)

        if (sb.isNotEmpty()) {
            setEvents(sb.toString())
        }
    }

    fun getEvents(): String? {
        return getOmnitureDataValue(EVENTS) as String?
    }

    fun setProducts(s: String?) {
        cData[PRODUCTS] = s ?: ""
    }

    fun getProducts(): String? {
        return getOmnitureDataValue(PRODUCTS) as String?
    }

    fun setPurchaseID(s: String?) {
        cData[PURCHASE_ID] = s ?: ""
    }

    fun setCurrencyCode(s: String?) {
        cData[CURRENCY_CODE] = s ?: ""
    }

    fun trackLink(linkName: String?) {
        cData[LINK_NAME] = linkName ?: ""
        cData[LINK_TYPE] = "o"
        analyticsProvider.trackAction(linkName, cData)
    }

    fun track() {
        analyticsProvider.trackState(appState, cData)
    }

    fun pauseActivity() {
        analyticsProvider.onPauseActivity()
    }

    fun resumeActivity(activity: Activity) {
        analyticsProvider.onResumeActivity(activity)
    }

    fun getOmnitureDataValue(key: String): Any? {
        return cData[key]
    }

    companion object {

        private const val EVAR = "&&v"
        private const val PROP = "&&c"
        private const val EVENTS = "&&events"
        private const val PRODUCTS = "&&products"
        private const val CURRENCY_CODE = "&&cc"
        private const val PURCHASE_ID = "&&purchaseID"
        private const val LINK_NAME = "&&linkName"
        private const val LINK_TYPE = "&&linkType"

        private val defaultAnalyticsProvider = OmnitureAnalyticsProvider()
        private var testAnalyticsProvider: AnalyticsProvider? = null

        private val analyticsProvider: AnalyticsProvider
                get() = testAnalyticsProvider ?: defaultAnalyticsProvider

        @JvmStatic fun getUrlWithVisitorData(url: String?): String = analyticsProvider.getUrlWithVisitorData(url)

        @JvmStatic protected fun setAnalyticsProviderForTest(provider: AnalyticsProvider?) {
            testAnalyticsProvider = provider
        }
    }
}
