package com.expedia.bookings.services;

import com.expedia.bookings.data.feeds.FeedsResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface FeedsApi {

	@GET("/feeds/api/feeds")
	Observable<FeedsResponse> feeds(
		@Query("tuid") String tuid,
		@Query("expuserid") String expUserId);
}
