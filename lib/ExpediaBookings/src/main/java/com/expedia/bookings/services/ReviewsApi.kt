package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.HotelReviewTranslationResponse
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.data.hotels.HotelReviewsSummaryResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewsApi {

    @GET("/api/hotelreviews/hotel/{hotelId}")
    @Headers("Cache-Control: no-cache")
    fun hotelReviews(@Path("hotelId") hotelId: String,
                     @Query("sortBy") sort: String,
                     @Query("start") start: Int,
                     @Query("items") items: Int,
                     @Query("locale") locale: String?,
                     @Query("searchTerm") searchTerm: String?): Observable<HotelReviewsResponse>

    @GET("/api/hotelreviews/hotel/{hotelId}/summary")
    @Headers("Cache-Control: no-cache")
    fun hotelReviewsSummary(@Path("hotelId") hotelId: String): Observable<HotelReviewsSummaryResponse>

    @GET("/api/hotelreview/translate/{reviewId}/{lang}")
    @Headers("Cache-Control: no-cache")
    fun translate(@Path("reviewId") reviewId: String,
                  @Path("lang") languageCode: String): Observable<HotelReviewTranslationResponse>
}
