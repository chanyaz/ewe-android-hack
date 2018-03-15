package com.expedia.bookings.services.urgency

import com.expedia.bookings.data.urgency.UrgencyResponse
import retrofit2.http.GET
import retrofit2.http.Query
import io.reactivex.Observable

interface UrgencyApi {
    @GET("ticker/region?")
    fun compression(
        @Query("ids") ids: String,
        @Query("checkinDate") checkinDate: String? = null,
        @Query("checkoutDate") checkoutDate: String? = null,
        @Query("clientId") clientId: String,
        @Query("type") type: String = "compression"
    ): Observable<UrgencyResponse>
}
