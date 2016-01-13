package com.expedia.bookings.data.cars;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.expedia.bookings.data.SuggestionV4;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class SuggestionResponse extends TypeAdapter<SuggestionResponse> {

	public List<SuggestionV4> suggestions;

	@Override
	public void write(JsonWriter out, SuggestionResponse value) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Handle the different types of suggest requests(v1 & v3) that are called
	 */
	@Override
	public SuggestionResponse read(JsonReader reader) throws IOException {
		JsonToken token = reader.peek();
		SuggestionResponse suggestion = new SuggestionResponse();
		reader.beginObject();
		if (token.equals(JsonToken.BEGIN_OBJECT)) {
			while (reader.hasNext()) {
				String jsonTag = reader.nextName();
				if ("r".equals(jsonTag) || "sr".equals(jsonTag)) {
					Gson gson = new Gson();
					Type listType = new TypeToken<List<SuggestionV4>>() {
					}.getType();
					suggestion.suggestions = gson.fromJson(reader, listType);
				}
				else {
					reader.skipValue();
				}
			}
			reader.endObject();
		}

		return suggestion;
	}

}
