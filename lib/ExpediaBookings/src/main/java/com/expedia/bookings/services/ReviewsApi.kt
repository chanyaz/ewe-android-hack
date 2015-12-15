package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.HotelReviewsResponse
import retrofit.http.GET
import retrofit.http.Headers
import retrofit.http.Path
import retrofit.http.Query
import rx.Observable

public interface ReviewsApi {

    @GET("/api/hotelreviews/hotel/{hotelId}")
    @Headers("Cache-Control: no-cache")
    public fun hotelReviews(@Path("hotelId") hotelId: String,
                            @Query("sortBy") sort: String,
                            @Query("start") start: Int,
                            @Query("items") items: Int,
                            @Query("locale") locale: String): Observable<HotelReviewsResponse>

}
