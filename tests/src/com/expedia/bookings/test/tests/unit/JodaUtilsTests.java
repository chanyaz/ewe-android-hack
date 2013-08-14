package com.expedia.bookings.test.tests.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.test.AndroidTestCase;

import com.expedia.bookings.utils.JodaUtils;

/*
 * A class of tests intended to be a "safety cushion" for the 
 * JodaUtils class.
 */

public class JodaUtilsTests extends AndroidTestCase {

	public void testLocaleDateComparisons() {
		LocalDate date1 = LocalDate.now();
		LocalDate date2 = LocalDate.now();
		LocalDate date3 = LocalDate.now().plusDays(1);
		assertTrue(JodaUtils.isBeforeOrEquals(date1, date2));
		assertTrue(JodaUtils.isAfterOrEquals(date2, date1));
		assertTrue(JodaUtils.isAfterOrEquals(date3, date2));
		assertTrue(JodaUtils.isBeforeOrEquals(date2, date3));
	}

	public void testExpiredDateTime() {
		DateTime thePast = DateTime.now().minusMillis(1);
		assertTrue(JodaUtils.isExpired(thePast, 0));
		assertFalse(JodaUtils.isExpired(thePast, 3000));

		DateTime oneSecondAgo = DateTime.now().minusSeconds(1);
		assertTrue(JodaUtils.isExpired(oneSecondAgo, 99));
		assertFalse(JodaUtils.isExpired(oneSecondAgo, 2000));
	}

	public void testDateDifferences() {
		DateTime now = new DateTime(DateTime.now());
		DateTime oneDayFromNow = DateTime.now().plusDays(1);
		assertTrue(JodaUtils.daysBetween(now, oneDayFromNow) == 1);
		assertTrue(JodaUtils.daysBetween(oneDayFromNow, now) == -1);
		assertTrue(JodaUtils.daysBetween(now, now) == 0);

		DateTime nowPlusOneSecond = DateTime.now().plusSeconds(1);
		if (nowPlusOneSecond.getDayOfYear() == now.getDayOfYear() && nowPlusOneSecond.getYear() == now.getYear()) {
			assertTrue(JodaUtils.daysBetween(now, nowPlusOneSecond) == 0);
		}

		DateTime nowMinusOneSecond = DateTime.now().minusSeconds(1);
		if (nowPlusOneSecond.getDayOfYear() == now.getDayOfYear() && nowPlusOneSecond.getYear() == now.getYear()) {
			assertTrue(JodaUtils.daysBetween(now, nowMinusOneSecond) == 0);
		}
	}

	public void testFormatTimezone() {
		DateTime now = DateTime.now().withZone(DateTimeZone.forOffsetHours(-9));
		DateTimeZone dtz = DateTimeZone.forOffsetHours(10);
		DateTime changedTimeZone = new DateTime(now.getMillis(), dtz);
		assertTrue(JodaUtils.formatTimeZone(now).equals("-09:00"));
		assertTrue(JodaUtils.formatTimeZone(changedTimeZone).equals("+10:00"));
	}

	public void testFormatting() {
		if (getContext().getResources().getConfiguration().locale.equals(new Locale("en", "US"))) {
			DateTime now = DateTime.now();
			String dayOfWeek = now.dayOfWeek().getAsText();
			String monthOfYear = now.monthOfYear().getAsText();
			String monthShort = now.monthOfYear().getAsShortText();
			String dayOfMonth = now.dayOfMonth().getAsText();
			String year = now.year().getAsText();

			String nowLongDateFormat = JodaUtils.formatDateTime(getContext(), now, JodaUtils.FLAGS_LONG_DATE_FORMAT);
			String expectedLongString = dayOfWeek + ", " + monthOfYear + " "
					+ dayOfMonth + ", " + year;
			assertEquals(nowLongDateFormat, expectedLongString);

			String nowMediumDateFormat = JodaUtils
					.formatDateTime(getContext(), now, JodaUtils.FLAGS_MEDIUM_DATE_FORMAT);
			String expectedMediumString = monthShort + " " + dayOfMonth + ", " + year;
			assertEquals(nowMediumDateFormat, expectedMediumString);

			String dateFormat = JodaUtils.formatDateTime(getContext(), now, JodaUtils.FLAGS_DATE_FORMAT);
			String expectedDateString = now.monthOfYear().getAsString() + "/" + now.dayOfMonth().getAsString() + "/"
					+ now.year().getAsString();
			assertEquals(dateFormat, expectedDateString);
		}
	}

	/* Direct comparison of the original DateTime and the instantiation
	 * returned from getDateTime fails, so we're just comparing the toString() 
	 * output.
	 */
	public void testBundlingDateTime() {
		DateTime now = DateTime.now();
		Bundle b = new Bundle();
		String key = "key";
		JodaUtils.putDateTime(b, key, now);
		DateTime stored = JodaUtils.getDateTime(b, key);
		assertEquals(stored.toString(), now.toString());
	}

	public void testLocaleDateJSONIO() throws JSONException {
		LocalDate ld = LocalDate.now();
		JSONObject obj = new JSONObject();
		String key = "key";
		JodaUtils.putLocalDateInJson(obj, key, ld);
		LocalDate returned = JodaUtils.getLocalDateFromJsonBackCompat(obj, key, null);
		assertEquals(ld, returned);
	}

	/* Direct comparison of the original DateTime and the instantiation
	 * returned from getDateTimeFromJsonBackCompat fails, so we're just 
	 * comparing the toString() output.
	 */
	public void testDateTimeJSONIO() throws JSONException {
		DateTime dt = DateTime.now();
		JSONObject obj = new JSONObject();
		String key = "key";
		JodaUtils.putDateTimeInJson(obj, key, dt);
		DateTime returned = JodaUtils.getDateTimeFromJsonBackCompat(obj, key, null);
		assertEquals(dt.toString(), returned.toString());
	}

	public void testDateTimeListJSONIO() throws JSONException {
		List<DateTime> list = new ArrayList<DateTime>();
		DateTime dt = DateTime.now();
		list.add(dt);
		list.add(dt.plusMonths(6));
		list.add(dt.plusMonths(12));
		JSONObject obj = new JSONObject();
		String key = "key";
		JodaUtils.putDateTimeListInJson(obj, key, list);
		List<DateTime> returned = JodaUtils.getDateTimeListFromJsonBackCompat(obj, key, null);
		for (int i = 0; i < list.size(); i++) {
			assertEquals(list.get(i).toString(), returned.get(i).toString());
		}
	}
}