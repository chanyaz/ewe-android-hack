package com.expedia.bookings.services;

import com.expedia.bookings.data.rail.requests.RailCheckoutParams;
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel;
import com.expedia.bookings.data.rail.responses.RailCardsResponse;
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse;
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

	@POST("trip/create")
	@FormUrlEncoded
	Observable<RailCreateTripResponse> railCreateTrip(@Field("offerToken") String offerToken);

	@POST("trip/checkout")
	Observable<RailCheckoutResponse> railCheckout(@Body RailCheckoutParams params);

	@GET("rails/domain/api/v1/static/RailCards")
	Observable<RailCardsResponse> railCards(@Query("locale") String locale);

}
