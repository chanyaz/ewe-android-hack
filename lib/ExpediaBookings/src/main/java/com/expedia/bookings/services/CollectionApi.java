package com.expedia.bookings.services;

import com.expedia.bookings.data.collections.Collection;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface CollectionApi {

	@GET("/static/mobile/PhoneDestinations/{country}/collections_{locale}.json")
	Observable<Collection> phoneCollection(@Path("country") String country, @Path("locale") String locale);
}
