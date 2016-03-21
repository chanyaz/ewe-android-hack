package com.expedia.bookings.services;

import com.expedia.bookings.data.rail.requests.RailCheckoutRequest;
import com.expedia.bookings.data.rail.requests.RailDetailsRequest;
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel;
import com.expedia.bookings.data.rail.requests.RailValidateRequest;
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse;
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse;
import com.expedia.bookings.data.rail.responses.RailDetailsResponse;
import com.expedia.bookings.data.rail.responses.RailSearchResponse;
import com.expedia.bookings.data.rail.responses.RailValidateResponse;

import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

public interface RailApi {

	@POST("/rails/ecom/v1/shopping/search")
	Observable<RailSearchResponse> railSearch(@Body RailApiSearchModel railSearchRequest);

	@POST("/rails/ecom/v1/shopping/getDetails")
	Observable<RailDetailsResponse> railDetails(@Body RailDetailsRequest railDetailsRequest);

	@POST("/rails/ecom/v1/shopping/validateOffer")
	Observable<RailValidateResponse> railValidate(@Body RailValidateRequest railValidateRequest);

	@POST("/m/api/rails/trip/create")
	@FormUrlEncoded
	Observable<RailCreateTripResponse> railCreateTrip(@Field("offerToken") String offerToken);

	@POST("/m/api/rails/trip/checkout")
	Observable<RailCheckoutResponse> railCheckout(@Body RailCheckoutRequest railCheckoutRequest);
}
