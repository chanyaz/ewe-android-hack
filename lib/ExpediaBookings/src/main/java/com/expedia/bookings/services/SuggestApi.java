package com.expedia.bookings.services;

import com.expedia.bookings.data.cars.SuggestionResponse;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface SuggestApi {

	//TODO support other POS
	@GET("/hint/es/v3/ac/en_US/{query}")
	public Observable<SuggestionResponse> suggestV3(@Path("query") String query, @Query("type") int suggestionResultType);
}
