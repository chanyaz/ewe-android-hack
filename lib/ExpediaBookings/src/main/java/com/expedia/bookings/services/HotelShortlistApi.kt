package com.expedia.bookings.services

import com.expedia.bookings.data.hotelshortlist.HotelShortlistFetchResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface HotelShortlistApi {

    @GET("api/ucs/shortlist/detail/fetch")
    fun fetch(@Query("configId") configId: String): Observable<HotelShortlistFetchResponse>
}
