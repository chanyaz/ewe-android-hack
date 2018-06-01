package com.expedia.bookings.notification

import com.expedia.bookings.services.TNSServices

class PushNotificationUtilsV2 {
    companion object {
        @JvmStatic
        fun sendConfirmationNotificationReceived(service: TNSServices, notificationId: String) {
            service.notificationReceivedConfirmation(notificationId)
        }
    }
}
