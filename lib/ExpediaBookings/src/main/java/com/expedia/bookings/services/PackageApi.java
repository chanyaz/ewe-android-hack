package com.expedia.bookings.services;

import java.util.Map;

import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.packages.PackageCheckoutResponse;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.data.packages.PackageOffersResponse;
import com.expedia.bookings.data.packages.PackageSearchResponse;

import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
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
		@Query("toDate") String toDate,
		@Query("packagePIID") String packagePIID,
		@Query("searchProduct") String searchProduct,
		@Query("selectLegId") String selectLegId,
		@Query("selectedLegId") String selectedLegId,
		@Query("packageTripType") String packageTripType);

	@GET("/api/packages/hotelOffers")
	Observable<PackageOffersResponse> hotelOffers(
		@Query("productKey") String productKey,
		@Query("checkInDate") String checkInDate,
		@Query("checkOutDate") String checkOutDate);

	@GET("/m/api/hotel/info")
	Observable<HotelOffersResponse> hotelInfo(
		@Query("hotelId") String hotelId);

	@FormUrlEncoded
	@POST("/api/packages/createTrip")
	Observable<PackageCreateTripResponse> createTrip(
		@FieldMap Map<String, Object> queryParams);

	@FormUrlEncoded
	@POST("/api/packages/checkout")
	Observable<PackageCheckoutResponse> checkout(
		@FieldMap Map<String, Object> queryParams);
}
