package com.expedia.bookings.services;

import com.expedia.bookings.data.hotels.NearbyHotelResponse;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface NearbyHotelApi {

	@GET("/m/api/hotel/search")
	public Observable<NearbyHotelResponse> nearbyHotelSearch(
		@Query("latitude") String latitude,
		@Query("longitude") String longitude,
		@Query("room1") String count,
		@Query("checkInDate") String checkInDate,
		@Query("checkOutDate") String checkOutDate,
		@Query("sortOrder") String sortOrder);
}
