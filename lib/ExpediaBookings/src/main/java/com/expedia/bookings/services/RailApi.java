package com.expedia.bookings.services;

import com.expedia.bookings.data.rail.requests.RailDetailsRequest;
import com.expedia.bookings.data.rail.requests.RailSearchRequest;
import com.expedia.bookings.data.rail.requests.RailValidateRequest;
import com.expedia.bookings.data.rail.responses.RailDetailsResponse;
import com.expedia.bookings.data.rail.responses.RailSearchResponse;
import com.expedia.bookings.data.rail.responses.RailValidateResponse;

import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface RailApi {

	@POST("/rails/ecom/v1/shopping/search")
	Observable<RailSearchResponse> railSearch(@Body RailSearchRequest railSearchRequest);

	@POST("/rails/ecom/v1/shopping/getDetails")
	Observable<RailDetailsResponse> railDetails(@Body RailDetailsRequest railDetailsRequest);

	@POST("/rails/ecom/v1/shopping/validateOffer")
	Observable<RailValidateResponse> railValidate(@Body RailValidateRequest railValidateRequest);
}
