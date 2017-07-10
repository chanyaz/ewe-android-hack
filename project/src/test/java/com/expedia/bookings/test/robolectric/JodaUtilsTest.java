package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.os.Bundle;

import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.JodaUtils;

/*
 * A class of tests intended to be a "safety cushion" for the
 * JodaUtils class.
 */

@RunWith(RobolectricRunner.class)
public class JodaUtilsTest {

	// Helper variables
	private Location mLocation = null;
	private BillingInfo mBillingInfo = null;

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	@Before
	public void before() {
		if (mLocation == null) {
			mLocation = new Location();
			mLocation.setCity("San Francisco");
			mLocation.setCountryCode("USA");
			mLocation.setDescription("Cool description");
			mLocation.addStreetAddressLine("114 Sansome St.");
			mLocation.setPostalCode("94109");
			mLocation.setStateCode("CA");
			mLocation.setLatitude(37.7833);
			mLocation.setLongitude(122.4167);
			mLocation.setDestinationId("SF");
		}

		if (mBillingInfo == null) {
			mBillingInfo = new BillingInfo();
			mBillingInfo.setEmail("qa-ehcc@mobiata.com");
			mBillingInfo.setFirstName("JexperCC");
			mBillingInfo.setLastName("MobiataTestaverde");
			mBillingInfo.setNameOnCard(mBillingInfo.getFirstName() + " " + mBillingInfo.getLastName());
			mBillingInfo.setLocation(mLocation);
			mBillingInfo.setNumberAndDetectType("4111111111111111");
			mBillingInfo.setSecurityCode("111");
			mBillingInfo.setTelephone("4155555555");
			mBillingInfo.setTelephoneCountryCode("1");
		}
	}

	@Test
	public void testLocaleDateComparisons() {
		LocalDate date1 = LocalDate.now();
		LocalDate date2 = LocalDate.now();
		LocalDate date3 = LocalDate.now().plusDays(1);
		Assert.assertTrue(JodaUtils.isBeforeOrEquals(date1, date2));
		Assert.assertTrue(JodaUtils.isAfterOrEquals(date2, date1));
		Assert.assertTrue(JodaUtils.isAfterOrEquals(date3, date2));
		Assert.assertTrue(JodaUtils.isBeforeOrEquals(date2, date3));
	}

	@Test
	public void testExpiredDateTime() {
		DateTime thePast = DateTime.now().minusMillis(1);
		Assert.assertTrue(JodaUtils.isExpired(thePast, 0));
		Assert.assertFalse(JodaUtils.isExpired(thePast, 3000));

		DateTime oneSecondAgo = DateTime.now().minusSeconds(1);
		Assert.assertTrue(JodaUtils.isExpired(oneSecondAgo, 99));
		Assert.assertFalse(JodaUtils.isExpired(oneSecondAgo, 2000));
	}

	@Test
	public void testDateDifferences() {
		DateTime now = new DateTime(DateTime.now());
		DateTime oneDayFromNow = DateTime.now().plusDays(1);
		Assert.assertTrue(JodaUtils.daysBetween(now, oneDayFromNow) == 1);
		Assert.assertTrue(JodaUtils.daysBetween(oneDayFromNow, now) == -1);
		Assert.assertTrue(JodaUtils.daysBetween(now, now) == 0);

		DateTime nowPlusOneSecond = DateTime.now().plusSeconds(1);
		if (nowPlusOneSecond.getDayOfYear() == now.getDayOfYear()
			&& nowPlusOneSecond.getYear() == now.getYear()) {
			Assert.assertTrue(JodaUtils.daysBetween(now, nowPlusOneSecond) == 0);
		}

		DateTime nowMinusOneSecond = DateTime.now().minusSeconds(1);
		if (nowPlusOneSecond.getDayOfYear() == now.getDayOfYear()
			&& nowPlusOneSecond.getYear() == now.getYear()) {
			Assert.assertTrue(JodaUtils.daysBetween(now, nowMinusOneSecond) == 0);
		}
	}

	@Test
	public void testFormatTimezone() {
		DateTime now = DateTime.now().withZone(DateTimeZone.forOffsetHours(-9));
		DateTimeZone dtz = DateTimeZone.forOffsetHours(10);
		DateTime changedTimeZone = new DateTime(now.getMillis(), dtz);
		Assert.assertTrue(JodaUtils.formatTimeZone(now).equals("-09:00"));
		Assert.assertTrue(JodaUtils.formatTimeZone(changedTimeZone).equals("+10:00"));
	}

	@Test
	public void testFormatting() {
		if (getContext().getResources().getConfiguration().locale
			.equals(new Locale("en", "US"))) {
			DateTime now = DateTime.now();
			String dayOfWeek = now.dayOfWeek().getAsText();
			String monthOfYear = now.monthOfYear().getAsText();
			String monthShort = now.monthOfYear().getAsShortText();
			String dayOfMonth = now.dayOfMonth().getAsText();
			String year = now.year().getAsText();

			String nowLongDateFormat = JodaUtils.formatDateTime(getContext(),
				now, DateFormatUtils.FLAGS_LONG_DATE_FORMAT);
			String expectedLongString = dayOfWeek + ", " + monthOfYear + " "
				+ dayOfMonth + ", " + year;
			Assert.assertEquals(expectedLongString, nowLongDateFormat);

			String nowMediumDateFormat = JodaUtils.formatDateTime(getContext(),
				now, DateFormatUtils.FLAGS_MEDIUM_DATE_FORMAT);
			String expectedMediumString = monthShort + " " + dayOfMonth + ", "
				+ year;
			Assert.assertEquals(expectedMediumString, nowMediumDateFormat);

			String dateFormat = JodaUtils.formatDateTime(getContext(), now,
				DateFormatUtils.FLAGS_DATE_NUMERIC | DateFormatUtils.FLAGS_MEDIUM_DATE_FORMAT);
			String expectedDateString = now.monthOfYear().getAsString() + "/"
				+ now.dayOfMonth().getAsString() + "/" + now.year().getAsString();
			Assert.assertEquals(expectedDateString, dateFormat);
		}
	}

	/*
	 * Direct comparison of the original DateTime and the instantiation returned
	 * from getDateTime fails, so we're just comparing the toString() output.
	 */
	@Test
	public void testBundlingDateTime() {
		DateTime now = DateTime.now();
		Bundle b = new Bundle();
		String key = "key";
		JodaUtils.putDateTime(b, key, now);
		DateTime stored = JodaUtils.getDateTime(b, key);
		Assert.assertEquals(stored.toString(), now.toString());
	}

	@Test
	public void testLocaleDateJSONIO() throws JSONException {
		LocalDate ld = LocalDate.now();
		JSONObject obj = new JSONObject();
		String key = "key";
		JodaUtils.putLocalDateInJson(obj, key, ld);
		LocalDate returned = JodaUtils.getLocalDateFromJson(obj, key);
		Assert.assertEquals(ld, returned);
	}

	/*
	 * Direct comparison of the original DateTime and the instantiation returned
	 * from getDateTimeFromJsonBackCompat fails, so we're just comparing the
	 * toString() output.
	 */
	@Test
	public void testDateTimeJSONIO() throws JSONException {
		DateTime dt = DateTime.now();
		JSONObject obj = new JSONObject();
		String key = "key";
		JodaUtils.putDateTimeInJson(obj, key, dt);
		DateTime returned = JodaUtils.getDateTimeFromJsonBackCompat(obj, key,
			null);
		Assert.assertEquals(dt.toString(), returned.toString());
	}

	@Test
	public void testDateTimeListJSONIO() throws JSONException {
		List<DateTime> list = new ArrayList<DateTime>();
		DateTime dt = DateTime.now();
		list.add(dt);
		list.add(dt.plusMonths(6));
		list.add(dt.plusMonths(12));
		JSONObject obj = new JSONObject();
		String key = "key";
		JodaUtils.putDateTimeListInJson(obj, key, list);
		List<DateTime> returned = JodaUtils.getDateTimeListFromJsonBackCompat(
			obj, key, null);
		for (int i = 0; i < list.size(); i++) {
			Assert.assertEquals(list.get(i).toString(), returned.get(i).toString());
		}
	}

	/*
	 * Tests verifying JodaUtils implementations in ExpediaServices
	 */

	// 100 millisecond wait() ensures objects' member variables are initialized
	// from system (e.g. POS)
	@Test
	public void testTimeInFlightCheckoutParams() throws InterruptedException {
		ExpediaServices expediaServices = new ExpediaServices(getContext());
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		Money baseFare = new Money("100", "USD");

		FlightTrip flightTrip = new FlightTrip();
		flightTrip.setBaseFare(baseFare);
		flightTrip.setTotalPrice(baseFare);

		Itinerary itinerary = new Itinerary();
		List<Traveler> travelers = new ArrayList<Traveler>();

		TripBucketItemFlight flightItem = new TripBucketItemFlight(flightTrip, itinerary);

		LocalDate today = LocalDate.now();
		mBillingInfo.setExpirationDate(today);
		verifyExpirationDates(query, today);

		LocalDate tomorrow = today.plusDays(1);
		mBillingInfo.setExpirationDate(tomorrow);
		query = expediaServices.generateFlightCheckoutParams(flightItem, mBillingInfo, travelers);
		verifyExpirationDates(query, tomorrow);

		LocalDate nextMonth = today.plusMonths(1);
		mBillingInfo.setExpirationDate(nextMonth);
		query = expediaServices.generateFlightCheckoutParams(flightItem, mBillingInfo, travelers);
		verifyExpirationDates(query, nextMonth);

		LocalDate nextYear = today.plusYears(1);
		mBillingInfo.setExpirationDate(nextYear);
		query = expediaServices.generateFlightCheckoutParams(flightItem, mBillingInfo, travelers);
		verifyExpirationDates(query, nextYear);
	}

	/*
	 * Helper method for verifying ExpediaServices expiration dates
	 */
	private void verifyExpirationDates(List<BasicNameValuePair> query, LocalDate time) {
		boolean expDateRead = false, expMonthRead = false, expYearRead = false;
		String expectedOutput;
		for (int i = 0; i < query.size(); i++) {
			if (expDateRead && expMonthRead && expYearRead) {
				break;
			}
			BasicNameValuePair pair = query.get(i);
			if (pair.getName().equals("expirationDate")) {
				expectedOutput = JodaUtils.format(time, "MMyy");
				Assert.assertEquals(pair.getValue(), expectedOutput);
				expDateRead = true;
			}
			else if (pair.getName().equals("expirationDateMonth")) {
				expectedOutput = JodaUtils.format(time, "MM");
				Assert.assertEquals(pair.getValue(), expectedOutput);
				expMonthRead = true;

			}
			else if (pair.getName().equals("expirationDateYear")) {
				expectedOutput = JodaUtils.format(time, "yyyy");
				Assert.assertEquals(expectedOutput, pair.getValue());
				expYearRead = true;
			}
		}
	}

}
