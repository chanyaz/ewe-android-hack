package com.expedia.bookings.services;

import java.util.List;
import java.util.Map;

import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.packages.PackageCheckoutResponse;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.data.packages.PackageOffersResponse;
import com.expedia.bookings.data.packages.PackageSearchResponse;

import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface PackageApi {

	@FormUrlEncoded
	@POST("/getpackages/v1?forceNoRedir=1&packageType=fh")
	Observable<PackageSearchResponse> packageSearch(
		@FieldMap Map<String, Object> queryParams);

	@GET("/api/packages/hotelOffers")
	Observable<PackageOffersResponse> packageHotelOffers(
		@Query("productKey") String productKey,
		@Query("checkInDate") String checkInDate,
		@Query("checkOutDate") String checkOutDate,
		@Query("ratePlanCode") String ratePlanCode,
		@Query("roomTypeCode") String roomTypeCode,
		@Query("numberOfAdultTravelers") Integer numberOfAdultTravelers,
		@Query("childTravelerAge") Integer childTravelerAge);

	@GET("/m/api/hotel/info")
	Observable<HotelOffersResponse> hotelInfo(
		@Query("hotelId") String hotelId);

	@FormUrlEncoded
	@POST("/api/packages/createTrip")
	Observable<PackageCreateTripResponse> createTrip(
		@Field("productKey") String productKey,
		@Query("destinationId") String destId,
		@Query("roomOccupants[0].numberOfAdultGuests") int numberOfAdults,
		@Query("roomOccupants[0].infantsInSeat") boolean infantInSeat,
		@Query("roomOccupants[0].childGuestAge") List<Integer> childAges,
		@Query("mobileFlexEnabled") boolean flexEnabled);

	@FormUrlEncoded
	@POST("/api/packages/checkout")
	Observable<PackageCheckoutResponse> checkout(
		@FieldMap Map<String, Object> queryParams);
}
