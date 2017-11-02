package com.expedia.bookings.services;

import java.util.List;
import com.expedia.bookings.data.GaiaSuggestion;
import retrofit2.http.GET;
import retrofit2.http.Query;
import io.reactivex.Observable;

public interface GaiaSuggestApi {

	@GET("v1/features")
	Observable<List<GaiaSuggestion>> gaiaNearBy(
		@Query("lat") Double latitude,
		@Query("lng") Double longitude,
		@Query("limit") Integer limit,
		@Query("lob") String lob,
		@Query("sortBy") String sortBy,
		@Query("locale") String locale,
		@Query("site") Integer site,
		@Query("geoVersion") String version);
}
