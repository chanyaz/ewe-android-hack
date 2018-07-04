package com.expedia.bookings.notification

import android.content.Context

object PushNotifcationUtilAccessor : IPushNotifcationUtilAccessor {
    override fun locKeyForDesktopBooking(locKey: String): Boolean {
        return PushNotificationUtils.locKeyForDesktopBooking(locKey)
    }

    override fun generateDesktopBookingNotification(context: Context, fhid: Int, locKey: String, locKeyArgs: Array<String>?) {
        PushNotificationUtils.generateDesktopBookingNotification(context, fhid, locKey, locKeyArgs)
    }
}

interface IPushNotifcationUtilAccessor {
    fun locKeyForDesktopBooking(locKey: String): Boolean
    fun generateDesktopBookingNotification(context: Context, fhid: Int, locKey: String, locKeyArgs: Array<String>?)
}
