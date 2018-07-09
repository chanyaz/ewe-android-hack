package com.expedia.bookings.services;

import com.expedia.bookings.data.hotels.HotelSearchResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HotelPredictionApi {

    @GET("/m/api/hotel/search/v3?")
    Observable<HotelSearchResponse> nearbyHotelSearch(
            @Query("latitude") String latitude,
            @Query("longitude") String longitude,
            @Query("room1") String count,
            @Query("checkInDate") String checkInDate,
            @Query("checkOutDate") String checkOutDate,
            @Query("sortOrder") String sortOrder,
            @Query("filterUnavailable") String filterUnavailable);

}
