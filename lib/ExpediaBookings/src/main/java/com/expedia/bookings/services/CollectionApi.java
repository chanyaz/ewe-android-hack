package com.expedia.bookings.services;

import com.expedia.bookings.data.collections.CollectionResponse;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface CollectionApi {

	@GET("/static/mobile/LaunchDestinations/{country}/collections_{locale}.json?")
	public Observable<CollectionResponse> collections(@Path("country") String country, @Path("locale") String locale);
}
