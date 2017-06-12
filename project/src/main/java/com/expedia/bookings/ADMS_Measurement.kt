package com.expedia.bookings

import android.content.Context
import com.adobe.mobile.Analytics

class ADMS_Measurement {

    var events: String? = null
    var appState: String? = null
    var products: String? = null
    var purchaseID: String? = null
    var currencyCode: String? = null
    var offlineTrackingEnabled: Boolean = false
    var reportSuiteIDs: String? = null
    var trackingServer: String? = null
    var SSL: Boolean = false
    var debugLogging: Boolean = false
    val visitorID: String? = null

    private val cData = HashMap<String, Any>()

    fun getProp(i: Int): String? {
        return getOmnitureDataValue(EVAR + i) as String?
    }

    fun getEvar(i: Int): String? {
        return getOmnitureDataValue(EVAR + i) as String?
    }

    fun setOnline() {
        //TO-DO
    }

    fun clearTrackingQueue() {
        //TO-DO
    }

    fun setOffline() {
        //TO-DO
    }

    fun setEvar(i: Int, s: String?) {
        cData.put(EVAR + i, s ?: "")
    }

    fun setProp(i: Int, s: String?) {
        cData.put(PROP + i, s ?: "")
    }

    fun trackLink(o: Any?, o1: String?, s: String?, o2: Any?, o3: Any?) {
        //TO-DO
    }

    fun track() {
        Analytics.trackAction("", cData)
    }

    fun startActivity(sContext: Context) {
        //TO-DO
    }

    fun stopActivity() {
        //TO-DO
    }

    fun getOmnitureDataValue(key: String): Any? {
        return cData[key]
    }

    companion object {

        private val EVAR = "&&v"

        private val PROP = "&&c"

        @JvmStatic fun sharedInstance(sContext: Context): ADMS_Measurement {
            return ADMS_Measurement()
        }

        @JvmStatic fun sharedInstance(): ADMS_Measurement {
            return ADMS_Measurement()
        }
    }
}
