package com.expedia.bookings.data.cars;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class RateTermDeserializer implements JsonDeserializer<RateTerm> {
	@Override
	public RateTerm deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jdc)
		throws JsonParseException {
		return RateTerm.toEnum(jsonElement.getAsString());
	}
}
