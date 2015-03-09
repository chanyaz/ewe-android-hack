package com.expedia.bookings.services;

import java.util.Map;

import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXSearchResponse;

import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import rx.Observable;

public interface LXApi {

	/**
	 * LX Search api. API defaults to today + 15 days if no dates are provided.
	 *
	 * @param location  required
	 * @param startDate optional
	 * @param endDate   optional
	 */
	@GET("/lx/api/search")
	public Observable<LXSearchResponse> searchLXActivities(
		@Query("location") String location,
		@Query("startDate") String startDate,
		@Query("endDate") String endDate);

	@GET("/lx/api/activity")
	public Observable<ActivityDetailsResponse> activityDetails(
		@Query("activityId") String activityId,
		@Query("startDate") String startDate,
		@Query("endDate") String endDate);

	@GET("/m/api/lx/trip/create")
	public Observable<LXCreateTripResponse> createTrip(
		@QueryMap Map<String, Object> params
	);

	@GET("/m/api/lx/trip/checkout")
	public Observable<LXCheckoutResponse> checkout(
		@QueryMap Map<String, Object> params
	);
}
