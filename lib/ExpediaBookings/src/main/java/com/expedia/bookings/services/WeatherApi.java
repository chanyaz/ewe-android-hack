package com.expedia.bookings.services;

import java.util.List;

import com.expedia.bookings.data.weather.DailyForecastResponse;
import com.expedia.bookings.data.weather.WeatherLocationResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface WeatherApi {

	@GET("/locations/v1/cities/search")
	Observable<List<WeatherLocationResponse>> locationSearch(
		@Query("apikey") String apiKey,
		@Query("q") String query);

	@GET("/forecasts/v1/daily/10day/")
	Observable<DailyForecastResponse> getFiveDayForcast(
		@Query("apikey") String apiKey,
		@Query("locationkey") String locationKey);
}
