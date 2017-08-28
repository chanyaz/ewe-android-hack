package com.expedia.bookings.services;

import java.util.List;

import retrofit2.http.GET;
import rx.Observable;

public interface SatelliteApi {
	@GET("/m/api/config/feature")
	Observable<List<String>> getFeatureConfigs();
}
