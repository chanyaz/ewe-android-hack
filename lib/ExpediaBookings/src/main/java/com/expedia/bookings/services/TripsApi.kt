package com.expedia.bookings.services

import org.json.JSONObject

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import rx.Observable

interface TripsApi {

    @GET("/api/trips/{tripId}")
    fun tripDetails(
            @Path("tripId") tripId: String,
            @Query("useCache") cache: String
    ): Call<JSONObject>

    @POST("/api/trips/{tripId}?idtype=itineraryNumber")
    fun guestTrip(
            @Path("tripId") tripId: String,
            @Query("email") guestEmail: String,
            @Query("useCache") cache: String
    ): Observable<JSONObject>
}
