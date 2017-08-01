package com.expedia.bookings.services;

import com.expedia.bookings.data.AbstractItinDetailsResponse;

import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import io.reactivex.Observable;

public interface ItinTripApi {

	@POST("/api/trips/{tripId}")
	Observable<AbstractItinDetailsResponse> tripDetails(
		@Path("tripId") String tripId
	);

	@POST("/api/trips/{tripId}?idtype=itineraryNumber")
	Observable<AbstractItinDetailsResponse> guestTrip(
		@Path("tripId") String tripId,
		@Query("email") String guestEmail
	);

}
