package com.expedia.bookings.services

import com.expedia.bookings.data.flights.RichContentRequest
import com.expedia.bookings.data.flights.RichContentResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Please add Kong APIs here for Flights
interface KongFlightApi {
    @Headers("Content-Type: application/json")
    @POST("/m/api/flight/getRichContent")
    fun richContent(@Body request: RichContentRequest): Observable<RichContentResponse>
}
