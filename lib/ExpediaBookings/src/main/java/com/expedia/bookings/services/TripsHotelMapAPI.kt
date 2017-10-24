package com.expedia.bookings.services

import com.expedia.bookings.data.trips.EventbriteResponse
import com.expedia.bookings.data.trips.TcsResponse
import com.expedia.bookings.data.trips.Trail
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import rx.Observable

interface TripsHotelMapAPI {
    //TCS
    @Headers(
            "Accept: application/json",
            "key: 4f8ce657-ee06-4527-a8d8-4b207f8f0d62"
    )
    @GET("/x/tcs/service/travel/latLng")
    fun poiNearby(
            @Query("latitude") latitude: String,
            @Query("longitude") longitude: String,
            @Query("apk") apk: String,
            @Query("langId") langId: String,
            @Query("sections") sections: Array<String>,
            @Query("version") version: Int,
            @Query("useCache") useCache: Boolean
    ): Observable<TcsResponse>

    //Eventbrite
    @Headers(
            "Authorization: Bearer 2QUX55T6BLUQ25HNEKKB"
    )
    @GET("events/search")
    fun eventsNearby(
            @Query("location.latitude") latitude: Double,
            @Query("location.longitude") longitude: Double,
            @Query("location.within") location: String,
            @Query("start_date.range_start") start: String,
            @Query("start_date.range_end") end: String,
            @Query("expand") expand: String

    ): Observable<EventbriteResponse>

    //Trails
    @GET("/api/v1/trailheads.json")
    fun trailHeadsNearby(
            @Query("key") key: String,
            @Query("latitude") latitude: String,
            @Query("longitude") longitude: String,
            @Query("limit") limit: String,
            @Query("distance") distance: String
    ): Observable<Array<Trail>>
}