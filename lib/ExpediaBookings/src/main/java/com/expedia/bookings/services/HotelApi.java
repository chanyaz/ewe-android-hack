package com.expedia.bookings.services;

import java.util.Map;

import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelSearchResponse;

import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
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

	@FormUrlEncoded
	@POST("/m/api/hotel/search?sortOrder=ExpertPicks&resultsPerPage=200&pageIndex=0&filterUnavailable=true&enableSponsoredListings=true")
	Observable<HotelSearchResponse> search(
		@FieldMap Map<String, Object> queryParams);

	@GET("/m/api/hotel/info")
	Observable<HotelOffersResponse> info(
		@Query("hotelId") String propertyId);

	@GET("/m/api/hotel/offers")
	Observable<HotelOffersResponse> offers(
		@Query("checkInDate") String checkInDate,
		@Query("checkOutDate") String checkOutDate,
		@Query("room1") String travelers,
		@Query("hotelId") String propertyId);

	@FormUrlEncoded
	@POST("/api/m/trip/coupon")
	Observable<HotelCreateTripResponse> applyCoupon(
		@FieldMap Map<String, Object> queryParams);

	@FormUrlEncoded
	@POST("/m/api/hotel/trip/create")
	Observable<HotelCreateTripResponse> createTrip(
		@FieldMap Map<String, Object> queryParams);

	@FormUrlEncoded
	@POST("/m/api/hotel/trip/checkout")
	Observable<HotelCheckoutResponse> checkout(
		@FieldMap Map<String, Object> queryParams);
}
