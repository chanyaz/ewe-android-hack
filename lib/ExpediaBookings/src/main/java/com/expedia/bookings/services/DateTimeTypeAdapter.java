package com.expedia.bookings.services;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.expedia.bookings.utils.Strings;
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
		out.value(value.withZone(DateTimeZone.UTC).getZone().getOffset(0) / 1000);

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
				String name = reader.nextName();
				if (Strings.equals(name, "epochSeconds")) {
					epochSeconds = reader.nextLong();
				}
				else if (Strings.equals(name, "timeZoneOffsetSeconds")) {
					timezoneOffsetSeconds = reader.nextInt();
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
