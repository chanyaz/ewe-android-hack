package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.NewHotelSearchResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface HotelSatelliteApi {
    @GET("/m/api/hotel/search/v1?resultsPerPage=200&pagination=0")
    fun search(
            @Query("regionId") regionId: String?,
            @Query("selected") hotelId: String?,
            @Query("latitude") lat: Double?,
            @Query("longitude") lng: Double?,
            @Query("checkInDate") checkIn: String,
            @Query("checkOutDate") checkOut: String,
            @Query("room1") guestString: String,
            @Query("shopWithPoints") shopWithPoints: Boolean,
            @Query("sortOrder") sortOrder: String?,
            @QueryMap(encoded = true) filterParams: Map<String, String>,
            @Query("mctc") mctc: Int?,
            @Query("enableSponsoredListings") enableSponsoredListings: Boolean): Observable<NewHotelSearchResponse>
}
