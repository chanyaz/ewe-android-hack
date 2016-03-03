package com.expedia.bookings.services;

import com.expedia.bookings.data.SuggestionV4Response;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface SuggestApi {

	@GET("/api/v4/typeahead/{query}")
	Observable<SuggestionV4Response> suggestV4(
		@Path("query") String query,
		@Query("locale") String locale,
		@Query("regiontype") int suggestionResultType,
		@Query("dest") boolean isDest,
		@Query("features") String features,
		@Query("client") String client,
		@Query("lob") String lineOfBusiness);

	@GET("/api/v4/nearby/")
	Observable<SuggestionV4Response> suggestNearbyV4(
		@Query("locale") String locale,
		@Query("latlong") String latlong,
		@Query("siteid") int siteid,
		@Query("regiontype") int suggestionResultType,
		@Query("sortcriteria") String sort,
		@Query("client") String client,
		@Query("lob") String lineOfBusiness);
}
