package com.expedia.bookings.services;

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

}
