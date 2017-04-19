package com.expedia.bookings.services;

import com.expedia.bookings.data.SuggestionV4Response;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface SuggestApi {

	// siteId passed only for rails
	@GET("/api/v4/typeahead/{query}")
	Observable<SuggestionV4Response> suggestV4(
		@Path("query") String query,
		@Query("locale") String locale,
		@Query("regiontype") int suggestionResultType,
		@Query("dest") boolean isDest,
		@Query("features") String features,
		@Query("client") String client,
		@Query("lob") String lineOfBusiness,
		@Query("siteid") Integer siteId,
		@Query("max_results") Integer maxResults,
		@Query("guid") String guid);
}
