package com.expedia.bookings.services;

import com.expedia.bookings.data.SatelliteSearchResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
class SatelliteDeserializer implements JsonDeserializer<SatelliteSearchResponse> {
	@Override
	public SatelliteSearchResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		SatelliteSearchResponse testListResponse = new SatelliteSearchResponse();
		JsonArray jsonArray = json.getAsJsonArray();

		testListResponse.setSource(jsonArray.get(0).toString());

		for (int i = 0; i < jsonArray.size(); i++) {
			testListResponse.testList.add(jsonArray.get(i).toString());
		}
		return testListResponse;
	}
}