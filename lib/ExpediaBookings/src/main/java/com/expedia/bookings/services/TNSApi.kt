package com.expedia.bookings.services;

import com.expedia.bookings.data.TNSDeregister
import com.expedia.bookings.data.TNSRegisterDeviceResponse
import com.expedia.bookings.data.TNSRegisterUserDeviceFlightsRequestBody
import com.expedia.bookings.data.TNSRegisterUserDeviceRequestBody
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

	@POST("/m/api/deregister")
	fun deregisterUserDevice(
		@Body body: TNSDeregister
	): Observable<TNSRegisterDeviceResponse>
}