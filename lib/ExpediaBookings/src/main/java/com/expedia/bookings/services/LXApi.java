package com.expedia.bookings.services;

import java.util.Map;

import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXCreateTripParams;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXSearchResponse;

import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;
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
	@Headers("Cache-Control: no-cache")
	public Observable<LXSearchResponse> searchLXActivities(
		@Query("location") String location,
		@Query("startDate") String startDate,
		@Query("endDate") String endDate);

	@GET("/lx/api/activity")
	@Headers("Cache-Control: no-cache")
	public Observable<ActivityDetailsResponse> activityDetails(
		@Query("activityId") String activityId,
		@Query("location") String location,
		@Query("startDate") String startDate,
		@Query("endDate") String endDate);

	@Headers("Content-Type: application/json")
	@POST("/m/api/lx/trip/create")
	public Observable<LXCreateTripResponse> createTrip(
		@Body LXCreateTripParams createTripParams
	);

	@FormUrlEncoded
	@POST("/m/api/lx/trip/checkout")
	public Observable<LXCheckoutResponse> checkout(
		@FieldMap Map<String, Object> params
	);
}
