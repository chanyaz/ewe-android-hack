package com.expedia.bookings.unit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;
import org.junit.Test;

import com.expedia.bookings.utils.server.DateTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static junit.framework.Assert.assertEquals;

public class DateTimeTypeAdapterTests {
	@Test
	public void testFromJsonAndToJson() {
		DateTimeTypeAdapter adapter = new DateTimeTypeAdapter();
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, adapter)
			.create();

		String raw = "{\"raw\":\"2015-02-20T12:30:00-05:00\",\"localized\":\"Feb 20, 2015 12:30:00 PM\",\"epochSeconds\":1424453400,\"timeZoneOffsetSeconds\":-18000,\"localizedShortDate\":\"Fri, 20 Feb\"}";

		DateTime parsed = gson.fromJson(raw, DateTime.class);
		DateTime expected = new DateTime(1424453400000L, DateTimeZone.forOffsetMillis(-18000000));

		assertEquals(expected, parsed);


		JSONObject json = new JSONObject(gson.toJson(parsed));

		assertEquals("1424453400", String.valueOf(json.getLong("epochSeconds")));
		assertEquals("-18000", String.valueOf(json.getInt("timeZoneOffsetSeconds")));
	}
}