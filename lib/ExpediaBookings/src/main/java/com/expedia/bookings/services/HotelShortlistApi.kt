package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItemMetadata
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HotelShortlistApi {

    @GET("api/ucs/shortlist/detail/fetch")
    fun fetch(@Query("configId") configId: String): Observable<HotelShortlistResponse<HotelShortlistItem>>

    @POST("api/ucs/shortlist/save/{itemId}")
    fun save(@Body metadata: ShortlistItemMetadata,
             @Path("itemId") itemId: String,
             @Query("configId") configId: String,
             @Query("pageName") pageName: String): Observable<HotelShortlistResponse<ShortlistItem>>

    @POST("api/ucs/shortlist/remove/{itemId}")
    fun remove(@Body metadata: ShortlistItemMetadata,
               @Path("itemId") itemId: String,
               @Query("configId") configId: String,
               @Query("pageName") pageName: String): Observable<ResponseBody>
}
