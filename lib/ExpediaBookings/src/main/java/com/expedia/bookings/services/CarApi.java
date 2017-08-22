package com.expedia.bookings.services;

import java.util.Map;

import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarSearchResponse;

import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface CarApi {

	@GET("/m/api/cars/search/airport")
	Observable<CarSearchResponse> roundtripCarSearch(
		@Query("airportCode") String airportCode,
		@Query("pickupTime") String pickupTime,
		@Query("dropOffTime") String dropoffTime);

	@GET("/m/api/cars/search/location")
	Observable<CarSearchResponse> roundtripCarSearch(
		@Query("pickupLocation.lat") double pickupLocationLatitude,
		@Query("pickupLocation.lon") double pickupLocationLongitude,
		@Query("pickupTime") String pickupTime,
		@Query("dropOffTime") String dropoffTime,
		@Query("searchRadius") int searchRadius);

	@GET("/m/api/cars/trip/create")
	Observable<CarCreateTripResponse> createTrip(
		@Query("productKey") String productKey,
		@Query("expectedTotalFare") String expectedTotalFare);

	@FormUrlEncoded
	@POST("/m/api/cars/trip/checkout")
	Observable<CarCheckoutResponse> checkout(
		@FieldMap Map<String, Object> params);
}
