package com.expedia.bookings.services

import com.expedia.bookings.data.trips.TcsResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import rx.Observable

interface TripsHotelMapAPI {

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
}