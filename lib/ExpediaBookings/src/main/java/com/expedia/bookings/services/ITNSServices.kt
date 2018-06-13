package com.expedia.bookings.services

import com.expedia.bookings.data.Courier
import com.expedia.bookings.data.TNSFlight
import com.expedia.bookings.data.TNSRegisterDeviceResponse
import com.expedia.bookings.data.TNSUser
import com.google.gson.JsonObject
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

interface ITNSServices {
    fun registerForFlights(user: TNSUser, courier: Courier, flights: List<TNSFlight>, observer: Observer<TNSRegisterDeviceResponse>? = null): Disposable
    fun deregisterForFlights(user: TNSUser, courier: Courier)
    fun flightStatsCallback(jsonBody: JsonObject, observer: Observer<TNSRegisterDeviceResponse>?): Disposable
}
