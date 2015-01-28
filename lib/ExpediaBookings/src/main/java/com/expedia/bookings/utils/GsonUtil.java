package com.expedia.bookings.utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class GsonUtil {

	public static void putForJsonable(JSONObject obj, String key, Object src) {
		Gson gson = getGson();
		JSONObject value = new JSONObject(gson.toJson(src));
		try {
			obj.put(key, value);
		}
		catch (JSONException e) {
			throw new RuntimeException("GsonUtil.putForJsonable fail", e);
		}
	}

	public static <T> T getForJsonable(JSONObject obj, String key, Class<T> cls) {
		try {
			Gson gson = getGson();
			JSONObject json = obj.getJSONObject(key);
			return gson.fromJson(json.toString(), cls);
		}
		catch (JSONException e) {
			throw new RuntimeException("GsonUtil.getForJsonable failure", e);
		}
	}

	public static void putListForJsonable(JSONObject obj, String key, Object src) {
		Gson gson = getGson();
		JSONArray value = new JSONArray(gson.toJson(src));
		try {
			obj.put(key, value);
		}
		catch (JSONException e) {
			throw new RuntimeException("GsonUtil.putForJsonable fail", e);
		}
	}

	public static <T> List<T> getListForJsonable(JSONObject obj, String key, Type type) {
		try {
			Gson gson = getGson();
			JSONArray json = obj.getJSONArray(key);
			return gson.fromJson(json.toString(), type);
		}
		catch (JSONException e) {
			throw new RuntimeException("GsonUtil.getListForJsonable failure", e);
		}
	}

	private static Gson getGson() {
		return new GsonBuilder()
			.registerTypeAdapter(BigDecimal.class, new BigDecimalTypeAdapter())
			.create();
	}

	public static class BigDecimalTypeAdapter extends TypeAdapter<BigDecimal> {
		@Override
		public void write(JsonWriter out, BigDecimal value) throws IOException {
			out.beginObject();

			out.name("bigDecimalStringRepresentation");
			out.value(value.toString());

			out.endObject();
		}

		@Override
		public BigDecimal read(JsonReader reader) throws IOException {
			JsonToken token = reader.peek();
			String bigDecimalStringRepresentation = null;

			if (token.equals(JsonToken.BEGIN_OBJECT)) {
				reader.beginObject();
				while (!reader.peek().equals(JsonToken.END_OBJECT)) {
					String name = reader.nextName();
					if (Strings.equals(name, "bigDecimalStringRepresentation")) {
						bigDecimalStringRepresentation = reader.nextString();
					}
					else {
						reader.skipValue();
					}
				}
				reader.endObject();
			}

			return new BigDecimal(bigDecimalStringRepresentation);
		}
	}

}
