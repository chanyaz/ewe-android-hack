package com.expedia.bookings.services;

import java.util.Map;

import com.expedia.bookings.data.flights.FlightCreateTripResponse;

import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import io.reactivex.Observable;

public interface InsuranceApi {
	@FormUrlEncoded
	@POST("/m/api/insurance/add")
	Observable<FlightCreateTripResponse> addInsuranceToTrip(
		@FieldMap Map<String, Object> queryParams);

	@FormUrlEncoded
	@POST("/m/api/insurance/remove")
	Observable<FlightCreateTripResponse> removeInsuranceFromTrip(
		@FieldMap Map<String, Object> queryParams);
}
