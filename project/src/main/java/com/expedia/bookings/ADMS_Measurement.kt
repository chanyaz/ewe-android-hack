package com.expedia.bookings

import android.app.Activity
import android.content.Context
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureAnalyticsProvider
import java.util.Hashtable

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
        cData.put(EVAR + i, s ?: "")
    }

    fun getEvar(i: Int): String? {
        return getOmnitureDataValue(EVAR + i) as String?
    }

    fun setProp(i: Int, s: String?) {
        cData.put(PROP + i, s ?: "")
    }

    fun getProp(i: Int): String? {
        return getOmnitureDataValue(PROP + i) as String?
    }

    fun setEvents(s: String?) {
        cData.put(EVENTS, s ?: "")
    }

    fun getEvents(): String? {
        return getOmnitureDataValue(EVENTS) as String?
    }

    fun setProducts(s: String?) {
        cData.put(PRODUCTS, s ?: "")
    }

    fun getProducts(): String? {
        return getOmnitureDataValue(PRODUCTS) as String?
    }

    fun setPurchaseID(s: String?) {
        cData.put(PURCHASE_ID, s ?: "")
    }

    fun setCurrencyCode(s: String?) {
        cData.put(CURRENCY_CODE, s ?: "")
    }

    fun trackLink(linkURL: String?, linkType: String?, linkName: String?, contextData: Hashtable<String, Any>?, variables: Hashtable<String, Any>?) {
        cData.put(LINK_NAME, linkName ?: "")
        cData.put(LINK_TYPE, linkType ?: "")
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

        @JvmStatic fun sharedInstance(sContext: Context): ADMS_Measurement = ADMS_Measurement()

        @JvmStatic fun sharedInstance(): ADMS_Measurement = ADMS_Measurement()

        @JvmStatic fun getUrlWithVisitorData(url: String?): String = analyticsProvider.getUrlWithVisitorData(url)

        @JvmStatic protected fun setAnalyticsProviderForTest(provider: AnalyticsProvider?) {
            testAnalyticsProvider = provider
        }
    }
}
