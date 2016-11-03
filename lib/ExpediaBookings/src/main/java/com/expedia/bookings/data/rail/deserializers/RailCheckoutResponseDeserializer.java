package com.expedia.bookings.data.rail.deserializers;

import java.lang.reflect.Type;

import com.expedia.bookings.data.BaseApiResponse;
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse;
import com.expedia.bookings.data.rail.responses.RailCheckoutResponseWrapper;
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class RailCheckoutResponseDeserializer implements JsonDeserializer<RailCheckoutResponseWrapper> {

	@Override
	public RailCheckoutResponseWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		throws JsonParseException {
		RailCheckoutResponseWrapper checkoutResponseWrapper = new RailCheckoutResponseWrapper();
		Gson gson = new Gson();

		BaseApiResponse baseApiResponse = gson.fromJson(json, BaseApiResponse.class);
		if (baseApiResponse.hasPriceChange()) {
			checkoutResponseWrapper.createTripResponse = gson.fromJson(json, RailCreateTripResponse.class);
		}
		else {
			checkoutResponseWrapper.checkoutResponse = gson.fromJson(json, RailCheckoutResponse.class);
		}

		return checkoutResponseWrapper;
	}
}
