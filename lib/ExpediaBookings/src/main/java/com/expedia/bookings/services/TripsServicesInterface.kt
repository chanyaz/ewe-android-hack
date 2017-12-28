package com.expedia.bookings.services

import org.json.JSONObject

interface TripsServicesInterface {
    fun getTripDetails(tripId: String, useCache: Boolean): JSONObject?
    fun getSharedTripDetails(sharedTripUrl: String): JSONObject?
    fun getGuestTrip(tripId: String, guestEmail: String, useCache: Boolean): JSONObject?
}