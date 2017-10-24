package com.expedia.bookings.services

import com.expedia.bookings.data.trips.EventbriteResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import rx.Observable

interface EventbriteAPI {
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
}