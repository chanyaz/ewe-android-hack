package com.expedia.bookings.unit;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.expedia.bookings.services.LocalDateTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import static junit.framework.Assert.assertEquals;

public class LocalDateTypeAdapterTests {
	private Gson mGson;
	private static final String PATTERN = "yyyy-MM-dd";

	@Before
	public void setupGson() {
		LocalDateTypeAdapter adapter = new LocalDateTypeAdapter(PATTERN);
		mGson = new GsonBuilder()
			.registerTypeAdapter(LocalDate.class, adapter)
			.create();
	}

	@Test
	public void testFromJsonAndToJson() {
		LocalDate date = new LocalDate("2015-11-22");
		String jsonString = mGson.toJson(date);
		LocalDate generatedDate = mGson.fromJson(jsonString, LocalDate.class);
		assertEquals(date, generatedDate);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testParsingNestedData() {
		String raw = "{\"foo\": {}}";
		LocalDate parsed = mGson.fromJson(raw, LocalDate.class);
	}

	@Test(expected = JsonSyntaxException.class)
	public void testParsingGarbageArray() {
		String raw = "[]";
		LocalDate parsed = mGson.fromJson(raw, LocalDate.class);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testParsingGarbageData() {
		String raw = "{\"foo\": 100}";
		LocalDate parsed = mGson.fromJson(raw, LocalDate.class);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testParsingGarbageObject() {
		String raw = "{}";
		LocalDate parsed = mGson.fromJson(raw, LocalDate.class);
	}
}
