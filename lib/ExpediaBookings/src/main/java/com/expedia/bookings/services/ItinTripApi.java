package com.expedia.bookings.services;

import com.expedia.bookings.data.AbstractItinDetailsResponse;

import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

public interface ItinTripApi {

	@POST("/api/trips/{tripId}")
	Observable<AbstractItinDetailsResponse> tripDetails(
		@Path("tripId") String tripId
	);
}
