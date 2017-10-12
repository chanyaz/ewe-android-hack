package com.expedia.bookings.services

import com.expedia.bookings.data.flights.KrazyGlueResponse
import retrofit2.http.GET
import retrofit2.http.Url
import rx.Observable

interface KrazyGlueApi {

    @GET
    fun getKrazyGlueHotels(@Url signedUrl: String): Observable<KrazyGlueResponse>
}
