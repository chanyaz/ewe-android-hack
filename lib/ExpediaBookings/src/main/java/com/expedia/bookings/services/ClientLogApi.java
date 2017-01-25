package com.expedia.bookings.services;

import java.util.Map;

import com.expedia.bookings.data.clientlog.EmptyResponse;
import com.expedia.bookings.utils.ClientLogConstants;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;

public interface ClientLogApi {

	@GET(ClientLogConstants.CLIENT_LOG_URL)
	Observable<EmptyResponse> log(
		@Query("pageName") String pageName,
		@Query("eventName") String eventName,
		@Query("domain") String domain,
		@Query("device") String deviceName,
		@Query("requestTime") long requestTime,
		@Query("responseTime") long responseTime,
		@Query("processingTime") long processingTime,
		@Query("requestToUser") long requestToUser,
		@Query("DeviceType") String deviceType);

	@POST("/cl/data/app-impressions.json?batch=false")
	Observable<EmptyResponse> deepLinkMarketingIdlog(
		@QueryMap Map<String, String> queryParams);
}
