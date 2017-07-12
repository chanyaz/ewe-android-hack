package com.expedia.bookings.services;

import java.util.List;
import java.util.Map;

import com.expedia.bookings.data.flights.FlightCheckoutResponse;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.flights.FlightSearchResponse;

import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface FlightApi {

	@FormUrlEncoded
	@POST("/api/flight/search?maxOfferCount=1600&lccAndMerchantFareCheckoutAllowed=true")
	Observable<FlightSearchResponse> flightSearch(
		@FieldMap Map<String, Object> queryParams, @Query("childTravelerAge") List<Integer> childAges,
		@Query("cabinClassPreference") String cabinClass, @Field("ul") Integer legNo,
		@Field("fl0") String selectedOutboundId,
		@Query("showRefundableFlight") Boolean showRefundableFlight,
		@Query("nonStopFlight") Boolean nonStopFlight,
		@Query("featureOverride") String featureOverride);

	@FormUrlEncoded
	@POST("/api/flight/trip/create?withInsurance=true")
	Observable<FlightCreateTripResponse> createTrip(
		@Query("mobileFlexEnabled") boolean flexEnabled,
		@FieldMap Map<String, Object> queryParams,
		@Query("featureOverride") String featureOverride);

	@FormUrlEncoded
	@POST("/api/flight/checkout")
	Observable<FlightCheckoutResponse> checkout(
		@FieldMap Map<String, Object> queryParams);
}
