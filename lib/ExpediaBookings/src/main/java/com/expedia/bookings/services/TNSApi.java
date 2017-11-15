package com.expedia.bookings.services;

import com.expedia.bookings.data.flights.TNSRegisterDeviceResponse;
import com.expedia.bookings.data.flights.TNSRegisterUserDeviceRequestBody;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by napandey on 11/15/17.
 */
interface TNSApi {

    @Headers("Content-Type: application/json")
    @POST("/register/user/device")
    Observable<TNSRegisterDeviceResponse> registerUserDevice(
            @Body TNSRegisterUserDeviceRequestBody body
    );

    @POST("/register/user/device/flights")
    Observable<TNSRegisterDeviceResponse> registerUserDeviceFlights(
            @Body TNSRegisterUserDeviceRequestBody body
    );
}