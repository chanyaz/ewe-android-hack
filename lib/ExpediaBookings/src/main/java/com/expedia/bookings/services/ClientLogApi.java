package com.expedia.bookings.services;

import com.expedia.bookings.data.clientlog.EmptyResponse;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface ClientLogApi {

	@GET("/cl/1x1.gif?live=true")
	Observable<EmptyResponse> log(
		@Query("pageName") String pageName,
		@Query("eventName") String eventName,
		@Query("domain") String domain,
		@Query("device") String deviceName,
		@Query("requestTime") long requestTime,
		@Query("responseTime") long responseTime,
		@Query("processingTime") long processingTime,
		@Query("requestToUser") long requestToUser);
}
