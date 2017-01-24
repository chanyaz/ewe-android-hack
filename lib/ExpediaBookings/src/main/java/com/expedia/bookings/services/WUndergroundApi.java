package com.expedia.bookings.services;

import com.expedia.bookings.data.itin.WUndergroundSearchResponse;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;

public interface WUndergroundApi {

	@Headers("Cache-Control: max-age=86400")
	@GET("/cgi-bin/findweather/getForecast")
	Observable<WUndergroundSearchResponse> getWeather(
		@Query("brand") String brand,
		@Query("query") String query);

}
