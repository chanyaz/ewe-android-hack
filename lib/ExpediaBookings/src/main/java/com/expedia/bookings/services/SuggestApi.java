package com.expedia.bookings.services;

import com.expedia.bookings.data.SuggestionV4Response;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import io.reactivex.Observable;

public interface SuggestApi {

	@GET("/api/v4/typeahead/{query}")
	Observable<SuggestionV4Response> suggestV4(
		@Path("query") String query,
		@Query("regiontype") int suggestionResultType,
		@Query("dest") boolean isDest,
		@Query("features") String features,
		@Query("lob") String lineOfBusiness,
		@Query("max_results") Integer maxResults,
		@Query("guid") String guid,
		@Query("pt") String packageType,
		@Query("ab") String abTest);

	@GET("/api/v4/ping")
	@Headers("Accept: text/html")
	Observable<ResponseBody> resolveEssDomain();
}
