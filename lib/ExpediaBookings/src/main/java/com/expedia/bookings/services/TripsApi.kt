package com.expedia.bookings.services

import io.reactivex.Observable
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface TripsApi {

    @GET("/api/trips/{tripId}")
    fun tripDetails(
            @Path("tripId") tripId: String,
            @Query("useCache") cache: String
    ): Call<JSONObject>

    @GET
    fun sharedTripDetails(
            @Url sharedTripUrl: String
    ): Call<JSONObject>

    @GET("/api/trips/{tripId}?idtype=itineraryNumber")
    fun guestTrip(
            @Path("tripId") tripId: String,
            @Query("email") guestEmail: String,
            @Query("useCache") cache: String
    ): Call<JSONObject>

    @GET("/api/trips/{tripId}")
    fun tripDetailsObservable(
            @Path("tripId") tripId: String,
            @Query("useCache") cache: String
    ): Observable<JSONObject>

    @GET
    fun sharedTripDetailsObservable(
            @Url sharedTripUrl: String
    ): Observable<JSONObject>

    @GET("/api/trips/{tripId}?idtype=itineraryNumber")
    fun guestTripObservable(
            @Path("tripId") tripId: String,
            @Query("email") guestEmail: String,
            @Query("useCache") cache: String
    ): Observable<JSONObject>
}
