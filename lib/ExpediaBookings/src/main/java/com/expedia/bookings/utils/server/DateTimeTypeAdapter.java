package com.expedia.bookings.utils.server;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class DateTimeTypeAdapter extends TypeAdapter<DateTime> {

	@Override
	public void write(JsonWriter out, DateTime value) throws IOException {
		out.beginObject();

		out.name("epochSeconds");
		out.value(value.getMillis() / 1000);

		out.name("timeZoneOffsetSeconds");
		out.value(value.getZone().getStandardOffset(0) / 1000);

		out.endObject();
	}

	@Override
	public DateTime read(JsonReader reader) throws IOException {
		JsonToken token = reader.peek();
		long epochSeconds = 0;
		int timezoneOffsetSeconds = 0;

		if (token.equals(JsonToken.BEGIN_OBJECT)) {
			reader.beginObject();
			while (!reader.peek().equals(JsonToken.END_OBJECT)) {
				if (reader.peek().equals(JsonToken.NAME)) {
					switch (reader.nextName()) {
					case "epochSeconds": {
						epochSeconds = reader.nextLong();
						break;
					}
					case "timeZoneOffsetSeconds": {
						timezoneOffsetSeconds = reader.nextInt();
						break;
					}
					default: {
						reader.skipValue();
					}
					}
				}
				else {
					reader.skipValue();
				}
			}
			reader.endObject();
		}

		long millisFromEpoch = epochSeconds * 1000;
		int millisTimezoneOffsetSeconds = timezoneOffsetSeconds * 1000;
		return new DateTime(millisFromEpoch, DateTimeZone.forOffsetMillis(millisTimezoneOffsetSeconds));
	}
}
