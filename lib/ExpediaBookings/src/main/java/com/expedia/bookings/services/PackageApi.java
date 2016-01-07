package com.expedia.bookings.services;

import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.packages.PackageOffersResponse;
import com.expedia.bookings.data.packages.PackageSearchResponse;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface PackageApi {

	@GET("/getpackages/v1?packageType=fh")
	Observable<PackageSearchResponse> packageSearch(
		@Query("origin") String origin,
		@Query("destination") String destination,
		@Query("originId") String originId,
		@Query("destinationId") String destinationId,
		@Query("ftla") String ftla,
		@Query("ttla") String ttla,
		@Query("fromDate") String fromDate,
		@Query("toDate") String toDate);

	@GET("/api/packages/hotelOffers")
	Observable<PackageOffersResponse> hotelOffers(
		@Query("productKey") String productKey,
		@Query("checkInDate") String checkInDate,
		@Query("checkOutDate") String checkOutDate);

	@GET("/m/api/hotel/info")
	Observable<HotelOffersResponse> hotelInfo(
		@Query("hotelId") String hotelId);
}
