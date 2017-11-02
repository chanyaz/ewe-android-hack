package com.expedia.bookings.services

import com.expedia.bookings.data.flights.BaggageInfoResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface BaggageApi {
    @POST("/api/flight/baggagefees")
    fun baggageInfo(
            @Body queryParams: MutableList<HashMap<String, String>>
    ): Observable<BaggageInfoResponse>
}