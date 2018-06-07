package com.expedia.bookings.notification

import android.app.IntentService
import android.content.Intent
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.ItineraryManagerInterface
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.services.TNSServices
import com.expedia.bookings.utils.Ui
import com.mobiata.android.Log
import org.json.JSONObject
import javax.inject.Inject

class GCMIntentServiceV2 : IntentService("GCMIntentServiceV2: " + PushNotificationUtils.SENDER_ID) {
    var intenaryManager: ItineraryManagerInterface = ItineraryManager.getInstance()
    private val LOGGING_TAG = "GCMIntentServiceV2"

    lateinit var tnsService: TNSServices
        @Inject set

    override fun onCreate() {
        super.onCreate()
        Ui.getApplication(this).appComponent().inject(this)
    }

    public override fun onHandleIntent(intent: Intent) {
        Log.d(LOGGING_TAG, "onHandleIntent")
        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("data") && extras.containsKey("message")) {
                val data = JSONObject(extras.getString("data"))
                val message = JSONObject(extras.getString("message"))

                val type = data.optString("EVENT_TYPE")

                val fhid: Int
                val flightHistoryId = data.optString("fhid")
                fhid = if (flightHistoryId.isNotBlank()) {
                    Integer.parseInt(flightHistoryId)
                } else {
                    0
                }
                //The key to determine which string to use
                val locKey = message.optString("loc-key")

                //The title
                val titleKey = message.optString("title-loc-key")

                val locArgs = message.optJSONArray("loc-args")
                var locArgsStrings: Array<String?>? = null
                if (locArgs != null) {
                    locArgsStrings = arrayOfNulls<String>(locArgs.length())
                    for (i in 0 until locArgs.length()) {
                        locArgsStrings[i] = locArgs.getString(i)
                    }
                }

                val notificationID = data.optString("nid")
                if (!notificationID.isEmpty()) {
                    PushNotificationUtilsV2.sendConfirmationNotificationReceived(tnsService, notificationID)
                }

                //If our itins arent synced yet, we cant show pretty notifications, so if we are syncing, we wait
                if (!intenaryManager.isSyncing()) {
                    Log.d(LOGGING_TAG, "GCM onMessage - not syncing, starting sync.")
                    intenaryManager.startSync(true)
                    intenaryManager.addSyncListener(makeSyncListener(fhid, locKey, locArgsStrings, type, titleKey, notificationID))
                } else {
                    Log.d(LOGGING_TAG, "GCM onMessage - Waiting for the ItinManager to finish syncing...")
                    intenaryManager.addSyncListener(makeSyncListener(fhid, locKey, locArgsStrings, type, titleKey, notificationID))
                }
            } else {
                val errorBuilder = StringBuilder("GCM - Missing Required Extras from fields: ")
                val errorList = mutableListOf<String>()
                if (!extras.containsKey("data")) {
                    errorList.add("data")
                }
                if (!extras.containsKey("message")) {
                    errorList.add("message")
                }
                errorBuilder.append(errorList.joinToString())
                Log.d(LOGGING_TAG, errorBuilder.toString())
            }
        } else {
            Log.d(LOGGING_TAG, "intent's extras = null")
        }
    }

    private fun makeSyncListener(fhid: Int, locKey: String, locArgs: Array<String?>?, type: String, titleKey: String, nID: String): ItineraryManager.ItinerarySyncAdapter {
        return object : ItineraryManager.ItinerarySyncAdapter() {
            override fun onSyncFinished(trips: Collection<Trip>) {
                Log.d(LOGGING_TAG, "GCM onMessage - ItinManager finished syncing, building notification now.")
                intenaryManager.removeSyncListener(this)
                PushNotificationUtils.generateNotification(this@GCMIntentServiceV2, fhid, locKey, locArgs, titleKey, nID, type)
            }

            override fun onSyncFailure(error: ItineraryManager.SyncError) {
                Log.d(LOGGING_TAG, "GCM onMessage - ItinManager failed syncing, building notification now.")
                intenaryManager.removeSyncListener(this)
                PushNotificationUtils.generateNotification(this@GCMIntentServiceV2, fhid, locKey, locArgs, titleKey, nID, type)
            }
        }
    }
}
