package com.expedia.bookings.services;

import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelSearchResponse;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface HotelApi {

	@GET("/m/api/hotel/search")
	Observable<HotelSearchResponse> nearbyHotelSearch(
		@Query("latitude") String latitude,
		@Query("longitude") String longitude,
		@Query("room1") String count,
		@Query("checkInDate") String checkInDate,
		@Query("checkOutDate") String checkOutDate,
		@Query("sortOrder") String sortOrder,
		@Query("filterUnavailable") String filterUnavailable);

	@GET("/m/api/hotel/search?sourceType=mobileApp&sortOrder=ExpertPicks&resultsPerPage=50&pageIndex=0&filterUnavailable=true&enableSponsoredListings=true")
	Observable<HotelSearchResponse> regionSearch(
		@Query("regionId") String regionId,
		@Query("checkInDate") String checkInDate,
		@Query("checkOutDate") String checkOutDate,
		@Query("room1") String travelers);

	@GET("/m/api/hotel/offers")
	Observable<HotelOffersResponse> offers(
		@Query("checkInDate") String checkInDate,
		@Query("checkOutDate") String checkOutDate,
		@Query("room1") String travelers,
		@Query("hotelId") String propertyId);
}
