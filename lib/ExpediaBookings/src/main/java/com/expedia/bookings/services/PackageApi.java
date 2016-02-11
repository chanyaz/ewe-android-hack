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

	@FormUrlEncoded
	@POST("/getpackages/v1?packageType=fh")
	Observable<PackageSearchResponse> packageSearch(
		@FieldMap Map<String, Object> queryParams);

	@GET("/api/packages/hotelOffers")
	Observable<PackageOffersResponse> packageHotelOffers(
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
