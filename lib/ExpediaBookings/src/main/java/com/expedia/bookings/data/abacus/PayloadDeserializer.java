package com.expedia.bookings.data.abacus;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class PayloadDeserializer implements JsonDeserializer<AbacusResponse> {
	@Override
	public AbacusResponse deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
		throws JsonParseException {
		AbacusResponse searchResponse = new AbacusResponse();
		JsonObject payload = je.getAsJsonObject().getAsJsonObject("payload");

		Type mapType = new TypeToken<Map<String, AbacusTest>>() {
		}.getType();
		Map<String, AbacusTest> map = new Gson().fromJson(payload, mapType);
		searchResponse.setAbacusTestMap(map);

		return searchResponse;
	}
}
