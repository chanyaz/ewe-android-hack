package com.expedia.bookings.services;

import java.util.Map;

import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarSearchResponse;

import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

public interface CarApi {

	@GET("/m/api/cars/search/airport")
	public Observable<CarSearchResponse> roundtripCarSearch(
		@Query("airportCode") String airportCode,
		@Query("pickupTime") String pickupTime,
		@Query("dropOffTime") String dropoffTime);

	@GET("/m/api/cars/search/location")
	public Observable<CarSearchResponse> roundtripCarSearch(
		@Query("pickupLocation.lat") double pickupLocationLatitude,
		@Query("pickupLocation.lon") double pickupLocationLongitude,
		@Query("pickupTime") String pickupTime,
		@Query("dropOffTime") String dropoffTime,
		@Query("searchRadius") int searchRadius);

	@GET("/m/api/cars/trip/create")
	public Observable<CarCreateTripResponse> createTrip(
		@Query("productKey") String productKey,
		@Query("expectedTotalFare") String expectedTotalFare);

	@FormUrlEncoded
	@POST("/m/api/cars/trip/checkout")
	public Observable<CarCheckoutResponse> checkout(
		@FieldMap Map<String, Object> params);
}
