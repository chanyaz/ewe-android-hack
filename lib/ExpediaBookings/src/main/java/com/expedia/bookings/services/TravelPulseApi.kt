package com.expedia.bookings.services

import com.expedia.bookings.data.travelpulse.TravelPulseFetchResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TravelPulseApi {

    @GET("/service/shortlist/detail/fetch/{siteId}")
    fun fetch(
            @Path("siteId") siteId: String,
            @Query("clientId") clientId: String,
            @Query("expUserId") expUserId: String,
            @Query("guid") guid: String,
            @Query("langId") langId: String,
            @Query("configId") configId: String?): Observable<TravelPulseFetchResponse>
}
