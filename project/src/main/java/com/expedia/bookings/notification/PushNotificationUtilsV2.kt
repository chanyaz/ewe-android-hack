package com.expedia.bookings.notification

import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.services.TNSServices
import com.mobiata.android.Log

class PushNotificationUtilsV2 {
    companion object {
        private val LOGGING_TAG = "PushNotificationUtilsV2"
        @JvmStatic
        fun sendConfirmationNotificationReceived(service: TNSServices, notificationId: String) {
            service.notificationReceivedConfirmation(notificationId)
        }

        @JvmStatic
        fun generateFlightAlertWithNoLocalizationNotification(notificationManager: INotificationManager, body: String, title: String, nID: String, dataFlight: ItinCardDataFlight?, type: String) {
            val itinId: String
            if (dataFlight != null) {
                itinId = dataFlight.id
            } else {
                itinId = "-1"
                Log.e(LOGGING_TAG, "generateNotification couldnt find ItinCardData is null")
            }

            val triggerTimeMillis = System.currentTimeMillis()

            val uniqueId = sanitizeUniqueId("Push_$nID")

            val notification = Notification(uniqueId, itinId, triggerTimeMillis)
            notification.notificationType = Notification.NotificationType.FLIGHT_ALERT
            notification.flags = Notification.FLAG_PUSH

            notification.iconResId = R.drawable.ic_stat_flight
            notification.imageType = Notification.ImageType.NONE
            notification.title = title
            notification.body = body
            notification.ticker = body
            notification.templateName = type

            notification.save()
            notificationManager.scheduleNotification(notification)
        }

        fun sanitizeUniqueId(uniqueId: String): String {
            var retStr = uniqueId.replace("\\W".toRegex(), "")
            if (retStr.length > 1024) {
                retStr = retStr.substring(0, 1024)
            }
            Log.d(LOGGING_TAG, "PushNotificationUtils.sanitizeUniqueId input:$uniqueId output:$retStr")
            return retStr
        }
    }
}
