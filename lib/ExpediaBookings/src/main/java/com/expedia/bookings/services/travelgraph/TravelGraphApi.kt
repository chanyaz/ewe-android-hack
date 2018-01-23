package com.expedia.bookings.services.travelgraph

import com.expedia.bookings.data.travelgraph.TravelGraphUserHistoryResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TravelGraphApi {

    @GET("travelGraphUserHistory/{siteId}/{locale}")
    fun fetchUserHistory(
            @Path("siteId") siteId: String,
            @Path("locale") locale: String,
            @Query("expUserId") expUserId: String,
            @Query("transactionGUID") transactionGUID: String,
            @Query("lobs") lobs: ArrayList<String>): Observable<TravelGraphUserHistoryResponse>
}
