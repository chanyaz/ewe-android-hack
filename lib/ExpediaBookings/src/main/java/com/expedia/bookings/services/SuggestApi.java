package com.expedia.bookings.services;

import com.expedia.bookings.data.cars.SuggestionResponse;
import com.expedia.bookings.data.hotels.SuggestionV4Response;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface SuggestApi {

	@GET("/hint/es/v3/ac/{locale}/{query}")
	public Observable<SuggestionResponse> suggestV3(@Path("query") String query,
		@Query("type") int suggestionResultType, @Path("locale") String locale, @Query("lob") String lineOfBusiness);

	@GET("/api/v4/typeahead/{query}")
	public Observable<SuggestionV4Response> suggestV4(
		@Path("query") String query,
		@Query("locale") String locale,
		@Query("regiontype") int suggestionResultType,
		@Query("features") String features,
		@Query("client") String client,
		@Query("lob") String lineOfBusiness);

	@GET("/hint/es/v1/nearby/{locale}")
	public Observable<SuggestionResponse> suggestNearbyV1(
		@Path("locale") String locale,
		@Query("latlong") String latlong,
		@Query("siteid") int siteid,
		@Query("type") int type,
		@Query("sort") String sort,
		@Query("lob") String lineOfBusiness);

	@GET("/api/v4/nearby/")
	public Observable<SuggestionV4Response> suggestNearbyV4(
		@Query("locale") String locale,
		@Query("latlong") String latlong,
		@Query("siteid") int siteid,
		@Query("regiontype") int suggestionResultType,
		@Query("sortcriteria") String sort,
		@Query("client") String client,
		@Query("lob") String lineOfBusiness);
}
