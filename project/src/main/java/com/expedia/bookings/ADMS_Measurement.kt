package com.expedia.bookings

import android.app.Activity
import android.content.Context
import com.adobe.mobile.Analytics
import com.adobe.mobile.Config

class ADMS_Measurement {

    var events: String? = null
    var appState: String? = null
    var products: String? = null
    var purchaseID: String? = null
    var currencyCode: String? = null
    var reportSuiteIDs: String? = null
    var trackingServer: String? = null
    var SSL: Boolean = false
    var debugLogging: Boolean = false

    val visitorID by lazy {
        Config.getUserIdentifier()
    }

    private val cData = HashMap<String, Any>()

    fun getProp(i: Int): String? {
        return getOmnitureDataValue(EVAR + i) as String?
    }

    fun getEvar(i: Int): String? {
        return getOmnitureDataValue(EVAR + i) as String?
    }

    fun setEvar(i: Int, s: String?) {
        cData.put(EVAR + i, s ?: "")
    }

    fun setProp(i: Int, s: String?) {
        cData.put(PROP + i, s ?: "")
    }

    fun trackLink(o: Any?, o1: String?, s: String?, o2: Any?, o3: Any?) {
        Analytics.trackAction(s, cData)
    }

    fun track() {
        Analytics.trackAction("", cData)
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

        @JvmStatic fun sharedInstance(sContext: Context): ADMS_Measurement {
            return ADMS_Measurement()
        }

        @JvmStatic fun sharedInstance(): ADMS_Measurement {
            return ADMS_Measurement()
        }
    }
}
