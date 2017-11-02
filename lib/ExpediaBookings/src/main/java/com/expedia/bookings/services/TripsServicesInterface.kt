package com.expedia.bookings.services

import io.reactivex.Observable
import org.json.JSONObject

interface TripsServicesInterface {
    fun getTripDetails(tripId: String, useCache: Boolean): JSONObject?
    fun getTripDetailsObservable(tripId: String, useCache: Boolean): Observable<JSONObject>
    fun getSharedTripDetails(sharedTripUrl: String): JSONObject?
    fun getSharedTripDetailsObservable(sharedTripUrl: String): Observable<JSONObject>
    fun getGuestTrip(tripId: String, guestEmail: String, useCache: Boolean): JSONObject?
    fun getGuestTripObservable(tripId: String, guestEmail: String, useCache: Boolean): Observable<JSONObject>
}
