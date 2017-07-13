package com.expedia.bookings.services;

import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface SatelliteApi {
	@GET("/m/api/config/feature")
	Observable<List<String>> getFeatureConfigs(
		@Query("clientid") String clientid);
}
