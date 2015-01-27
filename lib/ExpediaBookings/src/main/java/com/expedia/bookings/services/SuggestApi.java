package com.expedia.bookings.services;

import com.expedia.bookings.data.cars.SuggestionResponse;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface SuggestApi {

	//TODO support other POS
	@GET("/hint/es/v3/ac/en_US/{query}?type=1")
	public Observable<SuggestionResponse> suggestAirport(@Path("query") String query);
}
