package com.expedia.bookings.data.abacus;

import org.joda.time.DateTime;

import com.expedia.bookings.data.AbstractItinDetailsResponse;
import com.expedia.bookings.data.FlightItinDetailsResponse;
import com.expedia.bookings.data.HotelItinDetailsResponse;
import com.expedia.bookings.data.ItinDetailsResponse;
import com.expedia.bookings.services.DateTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class ItinTripDeserializer implements JsonDeserializer<AbstractItinDetailsResponse> {
	@Override
	public AbstractItinDetailsResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonObject responseData = object.getAsJsonObject("responseData");
		Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter()).create();
		if (responseData.has("hotels")) {
			return gson.fromJson(object, HotelItinDetailsResponse.class);
		}
		else if (responseData.has("flights")) {
			return gson.fromJson(object, FlightItinDetailsResponse.class);
		}
		else {
			return gson.fromJson(object, ItinDetailsResponse.class);
		}
	}
}
