package com.expedia.bookings.services

import com.expedia.bookings.data.trips.TripsShareUrlShortenResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable

interface TripShareUrlShortenAPI {

    @POST("/v1/shorten")
    fun shortenURL(
            @Body request: RequestBody
    ): Observable<TripsShareUrlShortenResponse>
}
