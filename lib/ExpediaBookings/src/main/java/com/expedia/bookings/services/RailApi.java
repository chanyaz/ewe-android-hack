package com.expedia.bookings.services;

import com.expedia.bookings.data.CardFeeResponse;
import com.expedia.bookings.data.rail.requests.RailCheckoutParams;
import com.expedia.bookings.data.rail.requests.RailCreateTripRequest;
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel;
import com.expedia.bookings.data.rail.responses.RailCardsResponse;
import com.expedia.bookings.data.rail.responses.RailCheckoutResponseWrapper;
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse;
import com.expedia.bookings.data.rail.responses.RailSearchResponse;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface RailApi {

	@POST("shop/search")
	Observable<RailSearchResponse> railSearch(@Body RailApiSearchModel railSearchRequest);

	@POST("domain/trip/createTrip")
	Observable<RailCreateTripResponse> railCreateTrip(@Body RailCreateTripRequest createTripRequest);

	@POST("trip/checkout")
	Observable<RailCheckoutResponseWrapper> railCheckout(@Body RailCheckoutParams params);

	@GET("domain/static/RailCards")
	Observable<RailCardsResponse> railCards(@Query("locale") String locale);

	@FormUrlEncoded
	@POST("trip/cardFee")
	Observable<CardFeeResponse> cardFees(@Field("tripId") String tripId,
		@Field("creditCardId") String creditCardId,
		@Field("tdoToken") String tdoToken);

}
