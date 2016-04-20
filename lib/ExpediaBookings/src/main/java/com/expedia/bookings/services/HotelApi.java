package com.expedia.bookings.services;

import java.util.Map;

import com.expedia.bookings.data.hotels.HotelCheckoutV2Params;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelSearchResponse;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface HotelApi {

	@GET("/m/api/hotel/search?forceV2Search=true")
	Observable<HotelSearchResponse> nearbyHotelSearch(
		@Query("latitude") String latitude,
		@Query("longitude") String longitude,
		@Query("room1") String count,
		@Query("checkInDate") String checkInDate,
		@Query("checkOutDate") String checkOutDate,
		@Query("sortOrder") String sortOrder,
		@Query("filterUnavailable") String filterUnavailable);

	@GET("/m/api/hotel/search?sortOrder=ExpertPicks&resultsPerPage=200&pageIndex=0&filterUnavailable=true&enableSponsoredListings=true&forceV2Search=true")
	Observable<HotelSearchResponse> search(
		@Query("regionId") String gaiaId,
		@Query("latitude") Double lat,
		@Query("longitude") Double lng,
		@Query("checkInDate") String checkIn,
		@Query("checkOutDate") String checkOut,
		@Query("room1") String guestString,
		@Query("shopWithPoints") Boolean shopWithPoints);

	@GET("/m/api/hotel/info?forceV2Search=true")
	Observable<HotelOffersResponse> info(
		@Query("hotelId") String propertyId);

	@GET("/m/api/hotel/offers?forceV2Search=true")
	Observable<HotelOffersResponse> offers(
		@Query("checkInDate") String checkInDate,
		@Query("checkOutDate") String checkOutDate,
		@Query("room1") String travelers,
		@Query("hotelId") String propertyId,
		@Query("shopWithPoints") Boolean shopWithPoints);

	@FormUrlEncoded
	@POST("/api/m/trip/coupon")
	Observable<HotelCreateTripResponse> applyCoupon(
		@FieldMap Map<String, Object> queryParams);

	@FormUrlEncoded
	@POST("/api/m/trip/remove/coupon")
	Observable<HotelCreateTripResponse> removeCoupon(
			@Field("tripId") String tripId);

	@FormUrlEncoded
	@POST("/m/api/hotel/trip/create")
	Observable<HotelCreateTripResponse> createTrip(
		@FieldMap Map<String, Object> queryParams);

	@POST("/m/api/hotel/trip/V2/checkout")
	Observable<HotelCheckoutResponse> checkout(@Body HotelCheckoutV2Params checkoutParamsV2);

}
