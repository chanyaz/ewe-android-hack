package com.expedia.bookings.services

import com.expedia.bookings.data.flights.KrazyglueResponse
import retrofit2.http.GET
import retrofit2.http.Url
import rx.Observable

interface KrazyglueApi {

    @GET
    fun getKrazyglueHotels(@Url signedUrl: String): Observable<KrazyglueResponse>
}
