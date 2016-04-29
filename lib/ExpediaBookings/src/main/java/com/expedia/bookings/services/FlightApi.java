package com.expedia.bookings.services;

import java.util.Map;

import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.flights.FlightSearchResponse;

import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface FlightApi {

	@FormUrlEncoded
	@POST("/api/flight/search?maxOfferCount=1600&lccAndMerchantFareCheckoutAllowed=true")
	Observable<FlightSearchResponse> flightSearch(
		@FieldMap Map<String, Object> queryParams);

	@FormUrlEncoded
	@POST("/api/flight/trip/create")
	Observable<FlightCreateTripResponse> createTrip(
		@FieldMap Map<String, Object> queryParams);

}
