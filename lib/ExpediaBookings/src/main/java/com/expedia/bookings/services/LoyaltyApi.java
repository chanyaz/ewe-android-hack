package com.expedia.bookings.services;

import com.expedia.bookings.data.payment.CalculatePointsResponse;
import com.expedia.bookings.data.payment.ProgramName;

import retrofit2.http.GET;
import retrofit2.http.Query;
import io.reactivex.Observable;

public interface LoyaltyApi {

	@GET("/m/api/trip/calculatePoints")
	Observable<CalculatePointsResponse> currencyToPoints(
		@Query("tripId") String tripId,
		@Query("programName") ProgramName programName,
		@Query("amount") String amount,
		@Query("rateId") String rateId
	);
}
