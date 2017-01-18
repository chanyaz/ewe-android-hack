package com.expedia.bookings.data.abacus;

import java.lang.reflect.Type;
import com.expedia.bookings.data.HotelItinDetailsResponse;
import com.expedia.bookings.data.ItinDetailsResponse;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ItinTripDeserializer implements JsonDeserializer<ItinDetailsResponse> {
	@Override
	public ItinDetailsResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonObject responseData = object.getAsJsonObject("responseData");
		if (responseData.has("hotels")) {
			return new Gson().fromJson(object, HotelItinDetailsResponse.class);
		}
		else {
			return new Gson().fromJson(object, ItinDetailsResponse.class);
		}
	}
}
