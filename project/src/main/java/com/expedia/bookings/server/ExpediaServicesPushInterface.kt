package com.expedia.bookings.server

import org.json.JSONObject

import com.expedia.bookings.data.PushNotificationRegistrationResponse

interface ExpediaServicesPushInterface {
    fun registerForPushNotifications(
        responseHandler: ResponseHandler<PushNotificationRegistrationResponse>,
        payload: JSONObject,
        regId: String
    ): PushNotificationRegistrationResponse?
}
