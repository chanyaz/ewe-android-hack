package com.expedia.bookings.data.clientlog;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface ClientLogApi {

	@GET("/cl/1x1.gif?live=true")
	Observable<EmptyResponse> log(
		@Query("pageName") String clientID,
		@Query("domain") String domain,
		@Query("device") String deviceName,
		@Query("logTime") String logTime,
		@Query("requestTime") long requestTime,
		@Query("responseTime") long responseTime,
		@Query("processingTime") long processingTime,
		@Query("requestToUser") long requestToUser);
}
