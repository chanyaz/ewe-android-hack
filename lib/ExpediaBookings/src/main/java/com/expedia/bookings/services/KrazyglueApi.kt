package com.expedia.bookings.services

import com.expedia.bookings.data.flights.KrazyglueResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

interface KrazyglueApi {

    @GET
    fun getKrazyglueHotels(@Url signedUrl: String): Observable<KrazyglueResponse>
}
