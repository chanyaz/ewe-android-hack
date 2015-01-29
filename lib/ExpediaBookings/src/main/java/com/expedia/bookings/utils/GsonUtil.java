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

	/**
	 * A utility method to be used in JSONable.toJson implementations that will
	 * persist the given object using Gson serialization. Ignores null Objects passed in.
	 *
	 * @param obj - JSONObject to store the serialized src Object
	 * @param key - JSON key for the given Object
	 * @param src - the class instance to persist
	 */
	public static void putForJsonable(JSONObject obj, String key, Object src) {
		if (src == null) {
			return;
		}

		try {
			Gson gson = getGson();
			String jsonStr = gson.toJson(src);
			JSONObject value = new JSONObject(jsonStr);
			obj.put(key, value);
		}
		catch (JSONException e) {
			throw new RuntimeException("GsonUtil.putForJsonable fail", e);
		}
	}

	/**
	 * A utility method to be used in JSONable.fromJson implementations that
	 * will deserialize an Object that was put in the JSONObject using the
	 * complementary putForJsonable(). Returns null if an entry does not
	 * exist for the provided key.
	 *
	 * @param obj - JSONObject in which the serialized Object is stored
	 * @param key - key of the Object to be unserialized
	 * @param cls - type of class to construct (required for Gson.fromJson)
	 * @param <T> - type of stored class instance
	 * @return - a new instance of the class constructed with the values stored within the JSONObject
	 */
	public static <T> T getForJsonable(JSONObject obj, String key, Class<T> cls) {
		try {
			Gson gson = getGson();
			JSONObject json = obj.optJSONObject(key);
			if (json == null) {
				return null;
			}
			return gson.fromJson(json.toString(), cls);
		}
		catch (JSONException e) {
			throw new RuntimeException("GsonUtil.getForJsonable failure", e);
		}
	}

	/**
	 * A utility method to be used in JSONAble.toJson implementations to store
	 * List<MyClass> member variables. Similar to putForJsonable but stores the
	 * List<MyClass> in a JSONArray rather than a JSONObject. Ignores nulls.
	 *
	 * @param obj - JSONObject to store the serialized src Object
	 * @param key - JSON key for the given Object
	 * @param src - the class instance to persist
	 */
	public static void putListForJsonable(JSONObject obj, String key, Object src) {
		if (src == null) {
			return;
		}

		try {
			Gson gson = getGson();
			JSONArray value = new JSONArray(gson.toJson(src));
			obj.put(key, value);
		}
		catch (JSONException e) {
			throw new RuntimeException("GsonUtil.putForJsonable fail", e);
		}
	}

	/**
	 * A utility method to be used in JSONAble.fromJson implementations that will
	 * deserialize a List of the given type from the provided JSONObject stored with
	 * key. Complementary to putListForJsonable(). Returns null if an entry does not
	 * exist for the provided key.
	 *
	 * @param obj  - JSONObject in which the serialized Object is stored
	 * @param key  - the key in obj in which to find the serialized Object
	 * @param type - type of class to construct (required for Gson.fromJson)
	 * @param <T>  - type of stored class instance
	 * @return
	 */
	public static <T> List<T> getListForJsonable(JSONObject obj, String key, Type type) {
		try {
			Gson gson = getGson();
			JSONArray json = obj.optJSONArray(key);
			if (json == null) {
				return null;
			}
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
