package com.expedia.bookings.services

import com.expedia.bookings.data.trips.TripsShareUrlShortenResponse
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface TripShareUrlShortenAPI {

    @POST("/v1/shorten")
    fun shortenURL(
            @Body request: RequestBody
    ): Observable<TripsShareUrlShortenResponse>
}
