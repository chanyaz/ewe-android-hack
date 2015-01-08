package com.expedia.bookings.utils.server;

import com.expedia.bookings.utils.data.cars.CarSearchResponse;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface CarApi {

	@GET("/airport")
	public void roundtripCarSearch(@Query("airportCode") String airportCode, @Query("pickupTime") String pickupTime,
								   @Query("dropOffTime") String dropoffTime, Callback<CarSearchResponse> callback);
}
