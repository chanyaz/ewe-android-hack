package com.expedia.bookings.itin.services

import com.expedia.bookings.itin.data.TNSRegisterDeviceResponse
import com.expedia.bookings.itin.data.TNSRegisterUserDeviceFlightsRequestBody
import com.expedia.bookings.itin.data.TNSRegisterUserDeviceRequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import rx.Observable

internal interface TNSApi {

    @Headers("Content-Type: application/json")
    @POST("/m/api/register/user")
    fun registerUserDevice(
            @Body body: TNSRegisterUserDeviceRequestBody
    ): Observable<TNSRegisterDeviceResponse>

    @POST("/m/api/register/user/flights")
    fun registerUserDeviceFlights(
            @Body body: TNSRegisterUserDeviceFlightsRequestBody
    ): Observable<TNSRegisterDeviceResponse>
}