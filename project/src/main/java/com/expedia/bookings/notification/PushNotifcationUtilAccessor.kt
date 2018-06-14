package com.expedia.bookings.notification

import android.content.Context
import com.expedia.bookings.data.trips.ItinCardDataFlight

object PushNotifcationUtilAccessor : IPushNotifcationUtilAccessor {
    override fun hasLocKeyForNewFlightAlerts(locKey: String): Boolean {
        return PushNotificationUtils.hasLocKeyForNewFlightAlerts(locKey)
    }

    override fun generateFlightAlertNotification(context: Context, fhid: Int, locKey: String, locKeyArgs: Array<String>?, titleArg: String, nID: String, data: ItinCardDataFlight) {
        PushNotificationUtils.generateFlightAlertNotification(context, fhid, locKey, locKeyArgs, titleArg, nID, data)
    }

    override fun locKeyForDesktopBooking(locKey: String): Boolean {
        return PushNotificationUtils.locKeyForDesktopBooking(locKey)
    }

    override fun generateDesktopBookingNotification(context: Context, fhid: Int, locKey: String, locKeyArgs: Array<String>?) {
        PushNotificationUtils.generateDesktopBookingNotification(context, fhid, locKey, locKeyArgs)
    }
}

interface IPushNotifcationUtilAccessor {
    fun hasLocKeyForNewFlightAlerts(locKey: String): Boolean
    fun generateFlightAlertNotification(context: Context, fhid: Int, locKey: String, locKeyArgs: Array<String>?, titleArg: String, nID: String, data: ItinCardDataFlight)
    fun locKeyForDesktopBooking(locKey: String): Boolean
    fun generateDesktopBookingNotification(context: Context, fhid: Int, locKey: String, locKeyArgs: Array<String>?)
}
