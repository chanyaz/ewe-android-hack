package com.expedia.bookings.services;

import java.util.Map;

import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.flights.FlightSearchResponse;

import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
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
