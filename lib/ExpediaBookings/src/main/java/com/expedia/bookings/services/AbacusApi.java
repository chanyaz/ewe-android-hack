package com.expedia.bookings.services;

import com.expedia.bookings.data.abacus.AbacusResponse;

import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;
import rx.Observable;

public interface AbacusApi {

	// How long to cache the response in milliseconds (24 hours) 60*60*24
	@Headers("Cache-Control: public, max-age=86400, max-stale=86400")
	@GET("/AB/layout")
	public Observable<AbacusResponse> downloadBucket(
		@Query("guid") String guid,
		@Query("site_id") String id
	);

}
