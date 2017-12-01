package com.expedia.bookings.itin.services;

import com.expedia.bookings.itin.data.TNSRegisterDeviceResponse;
import com.expedia.bookings.itin.data.TNSRegisterUserDeviceFlightsRequestBody;
import com.expedia.bookings.itin.data.TNSRegisterUserDeviceRequestBody;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

interface TNSApi {

	@Headers("Content-Type: application/json")
	@POST("/register/user/device")
	Observable<TNSRegisterDeviceResponse> registerUserDevice(
		@Body TNSRegisterUserDeviceRequestBody body
	);

	@POST("/register/user/device/flights")
	Observable<TNSRegisterDeviceResponse> registerUserDeviceFlights(
		@Body TNSRegisterUserDeviceFlightsRequestBody body
	);
}
