package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.HotelReviewsResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import io.reactivex.Observable

interface ReviewsApi {

    @GET("/api/hotelreviews/hotel/{hotelId}")
    @Headers("Cache-Control: no-cache")
    fun hotelReviews(@Path("hotelId") hotelId: String,
                     @Query("sortBy") sort: String,
                     @Query("start") start: Int,
                     @Query("items") items: Int,
                     @Query("locale") locale: String): Observable<HotelReviewsResponse>
}
