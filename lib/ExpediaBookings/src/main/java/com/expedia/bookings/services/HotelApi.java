package com.expedia.bookings.services;

import com.expedia.bookings.data.hotels.HotelSearchResponse;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface HotelApi {

	@GET("/m/api/hotel/search")
	public Observable<HotelSearchResponse> nearbyHotelSearch(
		@Query("latitude") String latitude,
		@Query("longitude") String longitude,
		@Query("room1") String count,
		@Query("checkInDate") String checkInDate,
		@Query("checkOutDate") String checkOutDate,
		@Query("sortOrder") String sortOrder);
}
