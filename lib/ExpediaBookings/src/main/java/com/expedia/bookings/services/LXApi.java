package com.expedia.bookings.services;

import com.expedia.bookings.data.lx.LXSearchResponse;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface LXApi {

	@GET("/lx/api/search")
	public Observable<LXSearchResponse> searchLXActivities(
		@Query("location") String location,
		@Query("startDate") String startDate,
		@Query("endDate") String endDate
	);
}
