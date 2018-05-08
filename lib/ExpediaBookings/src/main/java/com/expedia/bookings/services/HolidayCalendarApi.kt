package com.expedia.bookings.services

import com.expedia.bookings.data.HolidayCalendarResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface HolidayCalendarApi {
    @Headers("Content-Type: application/json")
    @GET("/m/api/calendar")
    fun holidayInfo(
            @Query("siteName") siteName: String,
            @Query("langId") langId: String
    ): Observable<HolidayCalendarResponse>
}
