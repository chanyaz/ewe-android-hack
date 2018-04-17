package com.expedia.bookings.services;

import java.math.BigDecimal;
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
import io.reactivex.Observable;

public interface FlightApi {

	@FormUrlEncoded
	@POST("api/flight/search?lccAndMerchantFareCheckoutAllowed=true")
	Observable<FlightSearchResponse> flightSearch(
		@FieldMap Map<String, Object> queryParams, @Query("childTravelerAge") List<Integer> childAges,
		@Query("cabinClassPreference") String cabinClass, @Field("ul") Integer legNo,
		@Field("fl0") String selectedOutboundId,
		@Query("showRefundableFlight") Boolean showRefundableFlight,
		@Query("nonStopFlight") Boolean nonStopFlight,
		@Query("featureOverride") String featureOverride,
		@Field("maxOfferCount") Integer maxOfferCount);

	@FormUrlEncoded
	@POST("api/flight/trip/create?withInsurance=true")
	Observable<FlightCreateTripResponse> oldCreateTrip(
		@Query("mobileFlexEnabled") boolean flexEnabled,
		@FieldMap Map<String, Object> queryParams,
		@Query("featureOverride") String featureOverride,
		@Query("fareFamilyCode") String fareFamilyCode,
		@Query("fareFamilyTotalPrice") BigDecimal fareFamilyTotalPrice);

	//Kong Create trip - Adding this to hit kong endpoint for create trip
	@FormUrlEncoded
	@POST("api/flight/trip?withInsurance=true")
	Observable<FlightCreateTripResponse> createTrip(
		@Query("mobileFlexEnabled") boolean flexEnabled,
		@FieldMap Map<String, Object> queryParams,
		@Query("featureOverride") String featureOverride,
		@Query("fareFamilyCode") String fareFamilyCode,
		@Query("fareFamilyTotalPrice") BigDecimal fareFamilyTotalPrice,
		@Field("childTravelerAge") List<Integer> childTravelerAge);

	@FormUrlEncoded
	@POST("api/flight/checkout")
	Observable<FlightCheckoutResponse> checkout(
		@FieldMap Map<String, Object> queryParams,
		@Query("featureOverride") String featureOverride);
}
