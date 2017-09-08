package com.expedia.bookings.services;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface SatelliteApi {
	@GET("/m/api/config/feature")
	Observable<List<String>> getFeatureConfigs();
}
