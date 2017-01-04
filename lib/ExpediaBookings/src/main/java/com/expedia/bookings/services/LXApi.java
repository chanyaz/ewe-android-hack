package com.expedia.bookings.services;

import com.expedia.bookings.data.lx.LXCreateTripResponseV2;
import java.util.Map;

import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXCreateTripParams;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXSearchResponse;

import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
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
	Observable<LXSearchResponse> searchLXActivities(
		@Query("location") String location,
		@Query("startDate") String startDate,
		@Query("endDate") String endDate);

	@GET("/lx/api/activity")
	@Headers("Cache-Control: no-cache")
	Observable<ActivityDetailsResponse> activityDetails(
		@Query("activityId") String activityId,
		@Query("location") String location,
		@Query("startDate") String startDate,
		@Query("endDate") String endDate);

	@Headers("Content-Type: application/json")
	@POST("/m/api/lx/trip/create")
	Observable<LXCreateTripResponse> createTrip(
		@Body LXCreateTripParams createTripParams
	);

	@Headers("Content-Type: application/json")
	@POST("/m/api/lx/trip/create")
	Observable<LXCreateTripResponseV2> createTripV2(
		@Body LXCreateTripParams createTripParams
	);

	@FormUrlEncoded
	@POST("/m/api/lx/trip/checkout")
	Observable<LXCheckoutResponse> checkout(
		@FieldMap Map<String, Object> params
	);
}
