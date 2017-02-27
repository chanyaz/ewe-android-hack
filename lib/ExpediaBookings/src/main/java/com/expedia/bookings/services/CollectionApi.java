package com.expedia.bookings.services;

import com.expedia.bookings.data.collections.Collection;
import com.expedia.bookings.data.collections.CollectionResponse;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import rx.Observable;

public interface CollectionApi {

	@GET("/static/mobile/LaunchDestinations/{country}/collections_{locale}.json")
	Observable<CollectionResponse> collections(@Path("country") String country, @Path("locale") String locale);

	@GET("/static/mobile/{phoneCollectionId}/{country}/collections_{locale}.json")
	@Headers("Cache-Control: max-age=15552000") // 15552000 seconds = 180 days
	Observable<Collection> phoneCollection(@Path("phoneCollectionId") String phoneCollectionId,
		@Path("country") String country, @Path("locale") String locale);
}
