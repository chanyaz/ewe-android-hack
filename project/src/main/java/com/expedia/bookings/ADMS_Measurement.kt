package com.expedia.bookings

import android.app.Activity
import android.content.Context
import com.adobe.mobile.Analytics
import com.adobe.mobile.Config

class ADMS_Measurement {

    var appState: String? = null
    var reportSuiteIDs: String? = null
    var trackingServer: String? = null
    var SSL: Boolean = false
    var debugLogging: Boolean = false

    val visitorID by lazy {
        Config.getUserIdentifier()
    }

    private val cData = HashMap<String, Any>()

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

    fun trackLink(o: Any?, o1: String?, s: String?, o2: Any?, o3: Any?) {
        Analytics.trackAction(s, cData)
    }

    fun track() {
        Analytics.trackState(appState, cData)
    }

    fun pauseActivity() {
        Config.pauseCollectingLifecycleData();
    }

    fun resumeActivity(activity: Activity) {
        Config.collectLifecycleData(activity);
    }

    fun getOmnitureDataValue(key: String): Any? {
        return cData[key]
    }

    companion object {

        private val EVAR = "&&v"

        private val PROP = "&&c"

        private val EVENTS = "&&events"

        private val PRODUCTS = "&&products"

        private val CURRENCY_CODE = "&&cc"

        private val PURCHASE_ID = "&&purchaseID"

        @JvmStatic fun sharedInstance(sContext: Context): ADMS_Measurement {
            return ADMS_Measurement()
        }

        @JvmStatic fun sharedInstance(): ADMS_Measurement {
            return ADMS_Measurement()
        }
    }
}
