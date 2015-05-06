package com.expedia.bookings.data.abacus;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class PayloadDeserializer implements JsonDeserializer<AbacusResponse> {
	@Override
	public AbacusResponse deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
		throws JsonParseException {
		AbacusResponse searchResponse = new AbacusResponse();
		JsonArray payload = je.getAsJsonObject().getAsJsonArray("evaluatedExperiments");
		Map<Integer, AbacusTest> map = new HashMap<>();

		for (JsonElement element : payload) {
			AbacusTest test = new Gson().fromJson(element.getAsJsonObject(), AbacusTest.class);
			map.put(test.id , test);
		}
		searchResponse.setAbacusTestMap(map);

		return searchResponse;
	}
}
