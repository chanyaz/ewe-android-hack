package com.expedia.bookings.services;

import com.expedia.bookings.data.CardFeeResponse;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface CardFeeApi {

	@FormUrlEncoded
	@POST("api/flight/trip/cardFee")
	Observable<CardFeeResponse> cardFee(@Field("tripId") String tripId,
		@Field("creditCardId") String creditCardId,
		@Query("featureOverride") String featureOverride);
}
