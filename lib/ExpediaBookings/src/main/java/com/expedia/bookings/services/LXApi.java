package com.expedia.bookings.services;

import com.expedia.bookings.data.lx.LXSearchResponse;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface LXApi {

	/**
	 * LX Search api. API defaults to today + 15 days if no dates are provided.
	 *
	 * @param location required
	 * @param startDate optional
	 * @param endDate optional
	 */
	@GET("/lx/api/search")
	public Observable<LXSearchResponse> searchLXActivities(
		@Query("location") String location,
		@Query("startDate") String startDate,
		@Query("endDate") String endDate
	);
}
