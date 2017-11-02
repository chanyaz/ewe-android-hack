package com.expedia.bookings.services;

import com.expedia.bookings.data.RoomUpgradeOffersResponse;

import retrofit2.http.GET;
import retrofit2.http.Url;
import io.reactivex.Observable;

public interface RoomUpgradeOffersApi {

	@GET
	Observable<RoomUpgradeOffersResponse> fetchOffers(@Url String url);
}
