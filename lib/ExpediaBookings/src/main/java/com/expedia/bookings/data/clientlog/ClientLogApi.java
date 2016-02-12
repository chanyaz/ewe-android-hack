package com.expedia.bookings.data.clientlog;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface ClientLogApi {

	@GET("/cl/1x1.gif")
	Observable<EmptyResponse> log(
		@Query("pageName") String clientID,
		@Query("requestTime") String requestTime,
		@Query("responseTime") String responseTime,
		@Query("processingTime") String processingTime,
		@Query("requestToUser") String requestToUser);
}
