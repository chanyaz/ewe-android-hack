package com.expedia.bookings.unit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.expedia.bookings.services.DateTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import static junit.framework.Assert.assertEquals;

public class DateTimeTypeAdapterTests {
	private Gson mGson;

	@Before
	public void setupGson() {
		DateTimeTypeAdapter adapter = new DateTimeTypeAdapter();
		mGson = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, adapter)
			.create();
	}

	@Test
	public void testFromJsonAndToJson() {
		String raw = "{\"raw\":\"2015-02-20T12:30:00-05:00\",\"localized\":\"Feb 20, 2015 12:30:00 PM\",\"epochSeconds\":1424453400,\"timeZoneOffsetSeconds\":-18000,\"localizedShortDate\":\"Fri, 20 Feb\"}";

		DateTime parsed = mGson.fromJson(raw, DateTime.class);
		DateTime expected = new DateTime(1424453400000L, DateTimeZone.forOffsetMillis(-18000000));

		assertEquals(expected, parsed);

		JSONObject json = new JSONObject(mGson.toJson(parsed));

		assertEquals("1424453400", String.valueOf(json.getLong("epochSeconds")));

		assertEquals(DateTimeZone.UTC.getOffset(-18000), json.getInt("timeZoneOffsetSeconds"));
	}

	@Test
	public void testParsingExtraData() {
		String raw = "{\"ignore\": \"me\", \"raw\":\"2015-02-20T12:30:00-05:00\",\"localized\":\"Feb 20, 2015 12:30:00 PM\",\"epochSeconds\":1424453400,\"timeZoneOffsetSeconds\":-18000,\"localizedShortDate\":\"Fri, 20 Feb\"}";

		DateTime parsed = mGson.fromJson(raw, DateTime.class);
		DateTime expected = new DateTime(1424453400000L, DateTimeZone.forOffsetMillis(-18000000));
	}

	@Test
	public void testParsingNestedData() {
		String raw = "{\"foo\": {}}";
		DateTime parsed = mGson.fromJson(raw, DateTime.class);
	}

	@Test(expected = JsonIOException.class)
	public void testParsingGarbageArray() {
		String raw = "[]";
		DateTime parsed = mGson.fromJson(raw, DateTime.class);
	}

	@Test
	public void testParsingGarbageData() {
		String raw = "{\"foo\": 100}";
		DateTime parsed = mGson.fromJson(raw, DateTime.class);
	}

	@Test
	public void testParsingGarbageObject() {
		String raw = "{}";
		DateTime parsed = mGson.fromJson(raw, DateTime.class);
	}
}
