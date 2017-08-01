package com.expedia.bookings.services;

import com.expedia.bookings.data.collections.Collection;
import com.expedia.bookings.data.collections.CollectionResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import io.reactivex.Observable;

public interface CollectionApi {

	@GET("/static/mobile/LaunchDestinations/{country}/collections_{locale}.json")
	Observable<CollectionResponse> collections(@Path("country") String country, @Path("locale") String locale);

	@GET("/static/mobile/{phoneCollectionId}/{country}/collections_{locale}.json")
	Observable<Collection> phoneCollection(@Path("phoneCollectionId") String phoneCollectionId,
		@Path("country") String country, @Path("locale") String locale);
}
