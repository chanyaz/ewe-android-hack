package com.expedia.bookings.services;

import com.expedia.bookings.data.rail.requests.RailCheckoutRequest;
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel;
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse;
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse;
import com.expedia.bookings.data.rail.responses.RailSearchResponse;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface RailApi {

	@POST("/rails/domain/m/api/v1/search")
	Observable<RailSearchResponse> railSearch(@Body RailApiSearchModel railSearchRequest);

	@POST("/m/api/rails/trip/create")
	@FormUrlEncoded
	Observable<RailCreateTripResponse> railCreateTrip(@Field("offerToken") String offerToken);

	@POST("/m/api/rails/trip/checkout")
	Observable<RailCheckoutResponse> railCheckout(@Body RailCheckoutRequest railCheckoutRequest);
}
