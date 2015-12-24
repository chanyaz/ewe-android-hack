package com.expedia.bookings.services;

import com.expedia.bookings.data.hotels.PackageSearchResponse;

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
}
