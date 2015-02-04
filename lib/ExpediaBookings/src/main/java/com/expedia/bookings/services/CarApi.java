package com.expedia.bookings.services;

import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarSearchResponse;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface CarApi {

	@GET("/m/api/cars/search/airport")
	public Observable<CarSearchResponse> roundtripCarSearch(
		@Query("airportCode") String airportCode,
		@Query("pickupTime") String pickupTime,
		@Query("dropOffTime") String dropoffTime);

	@GET("/m/api/cars/trip/create")
	public Observable<CarCreateTripResponse> createTrip(
		@Query("productKey") String productKey,
		@Query("expectedTotalFare") String expectedTotalFare);

	@GET("/m/api/cars/trip/checkout")
	public Observable<CarCheckoutResponse> checkoutWithoutCreditCard(
		@Query("suppressFinalBooking") boolean suppressFinalBooking,
		@Query("tripId") String tripId,
		@Query("expectedTotalFare") String expectedTotalFare,
		@Query("expectedFareCurrencyCode") String currencyCode,
		@Query("mainMobileTraveler.phoneCountryCode") String phoneCountryCode,
		@Query("mainMobileTraveler.phone") String phoneNumber,
		@Query("mainMobileTraveler.email") String emailAddress,
		@Query("mainMobileTraveler.firstName") String firstName,
		@Query("mainMobileTraveler.lastName") String lastName);
}
