package com.expedia.bookings.test.tests.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.test.ApplicationTestCase;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.test.utils.DataUtils;
import com.expedia.bookings.utils.JodaUtils;

/*
 * A class of tests intended to be a "safety cushion" for the 
 * JodaUtils class.
 */

public class JodaUtilsTests extends ApplicationTestCase<ExpediaBookingApp> {

	private ExpediaBookingApp mApp = null;

	// Helper variables
	private LocalDate mNow = null;
	private Location mLocation = null;
	private BillingInfo mBillingInfo = null;

	public JodaUtilsTests() {
		super(ExpediaBookingApp.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		if (mApp == null) {
			createApplication();
			mApp = getApplication();
		}
		while (!mApp.isInitialized()) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
		if (mNow == null) {
			mNow = LocalDate.now();
		}
		if (mLocation == null) {
			mLocation = DataUtils.setUpLocation("San Francisco", "USA", "Cool description",
					"114 Sansome St.",
					"94109", "CA",
					37.7833, 122.4167, "SF");
		}
		if (mBillingInfo == null) {
			mBillingInfo = DataUtils.setUpBillingInfo("qa-ehcc@mobiata.com", "4155555555", "1",
					"JexperCC",
					"MobiataTestaverde", "4111111111111111", "111", mNow, mLocation);
		}
	}

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
		if (nowPlusOneSecond.getDayOfYear() == now.getDayOfYear()
				&& nowPlusOneSecond.getYear() == now.getYear()) {
			assertTrue(JodaUtils.daysBetween(now, nowPlusOneSecond) == 0);
		}

		DateTime nowMinusOneSecond = DateTime.now().minusSeconds(1);
		if (nowPlusOneSecond.getDayOfYear() == now.getDayOfYear()
				&& nowPlusOneSecond.getYear() == now.getYear()) {
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
		if (getContext().getResources().getConfiguration().locale
				.equals(new Locale("en", "US"))) {
			DateTime now = DateTime.now();
			String dayOfWeek = now.dayOfWeek().getAsText();
			String monthOfYear = now.monthOfYear().getAsText();
			String monthShort = now.monthOfYear().getAsShortText();
			String dayOfMonth = now.dayOfMonth().getAsText();
			String year = now.year().getAsText();

			String nowLongDateFormat = JodaUtils.formatDateTime(getContext(),
					now, JodaUtils.FLAGS_LONG_DATE_FORMAT);
			String expectedLongString = dayOfWeek + ", " + monthOfYear + " "
					+ dayOfMonth + ", " + year;
			assertEquals(expectedLongString, nowLongDateFormat);

			String nowMediumDateFormat = JodaUtils.formatDateTime(getContext(),
					now, JodaUtils.FLAGS_MEDIUM_DATE_FORMAT);
			String expectedMediumString = monthShort + " " + dayOfMonth + ", "
					+ year;
			assertEquals(expectedMediumString, nowMediumDateFormat);

			String dateFormat = JodaUtils.formatDateTime(getContext(), now,
					JodaUtils.FLAGS_DATE_FORMAT | JodaUtils.FLAGS_MEDIUM_DATE_FORMAT);
			String expectedDateString = now.monthOfYear().getAsString() + "/"
					+ now.dayOfMonth().getAsString() + "/" + now.year().getAsString();
			assertEquals(expectedDateString, dateFormat);
		}
	}

	/*
	 * Direct comparison of the original DateTime and the instantiation returned
	 * from getDateTime fails, so we're just comparing the toString() output.
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
		LocalDate returned = JodaUtils.getLocalDateFromJsonBackCompat(obj, key,
				null);
		assertEquals(ld, returned);
	}

	/*
	 * Direct comparison of the original DateTime and the instantiation returned
	 * from getDateTimeFromJsonBackCompat fails, so we're just comparing the
	 * toString() output.
	 */
	public void testDateTimeJSONIO() throws JSONException {
		DateTime dt = DateTime.now();
		JSONObject obj = new JSONObject();
		String key = "key";
		JodaUtils.putDateTimeInJson(obj, key, dt);
		DateTime returned = JodaUtils.getDateTimeFromJsonBackCompat(obj, key,
				null);
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
		List<DateTime> returned = JodaUtils.getDateTimeListFromJsonBackCompat(
				obj, key, null);
		for (int i = 0; i < list.size(); i++) {
			assertEquals(list.get(i).toString(), returned.get(i).toString());
		}
	}

	/*
	 * Tests verifying JodaUtils implementations in ExpediaServices
	 */

	// 100 millisecond wait() ensures objects' member variables are initialized
	// from system (e.g. POS)
	public void testTimeInFlightCheckoutParams() throws InterruptedException {
		ExpediaServices expediaServices = new ExpediaServices(getContext());
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		Money baseFare = DataUtils.setUpMoney("100", "USD");

		FlightTrip flightTrip = new FlightTrip();
		flightTrip.setBaseFare(baseFare);
		flightTrip.setTotalFare(baseFare);

		Itinerary itinerary = new Itinerary();
		List<Traveler> travelers = new ArrayList<Traveler>();

		TripBucketItemFlight flightItem = new TripBucketItemFlight(flightTrip, itinerary);

		mBillingInfo.setExpirationDate(mNow);
		verifyExpirationDates(query, mNow);

		LocalDate tomorrow = LocalDate.now().plusDays(1);
		mBillingInfo.setExpirationDate(tomorrow);
		query = expediaServices.generateFlightCheckoutParams(flightItem, mBillingInfo, travelers, 0);
		verifyExpirationDates(query, tomorrow);

		LocalDate nextMonth = LocalDate.now().plusMonths(1);
		mBillingInfo.setExpirationDate(nextMonth);
		query = expediaServices.generateFlightCheckoutParams(flightItem, mBillingInfo, travelers, 0);
		verifyExpirationDates(query, nextMonth);

		LocalDate nextYear = LocalDate.now().plusYears(1);
		mBillingInfo.setExpirationDate(nextYear);
		query = expediaServices.generateFlightCheckoutParams(flightItem, mBillingInfo, travelers, 0);
		verifyExpirationDates(query, nextYear);
	}

	public void testTimeInHotelCheckOutParams() throws InterruptedException {
		ExpediaServices expediaServices = new ExpediaServices(getContext());
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		HotelSearchParams params = new HotelSearchParams();
		Property property = new Property();
		Rate rate = new Rate();
		Money money = new Money();
		money.setCurrency("USD");
		rate.setTotalPriceWithMandatoryFees(money);
		String tripId = "1234567";
		String userId = "12345";
		long tuid = 1234567;

		mBillingInfo.setExpirationDate(mNow);
		query = expediaServices.generateHotelReservationParams(params, rate, mBillingInfo, tripId, userId,
				tuid);
		verifyExpirationDates(query, mNow);

		LocalDate tomorrow = LocalDate.now().plusDays(1);
		mBillingInfo.setExpirationDate(tomorrow);
		query = expediaServices.generateHotelReservationParams(params, rate, mBillingInfo, tripId, userId,
			tuid);
		verifyExpirationDates(query, tomorrow);

		LocalDate nextMonth = LocalDate.now().plusMonths(1);
		mBillingInfo.setExpirationDate(nextMonth);
		query = expediaServices.generateHotelReservationParams(params, rate, mBillingInfo, tripId, userId,
			tuid);
		verifyExpirationDates(query, nextMonth);

		LocalDate nextYear = LocalDate.now().plusYears(1);
		mBillingInfo.setExpirationDate(nextYear);
		query = expediaServices.generateHotelReservationParams(params, rate, mBillingInfo, tripId, userId,
			tuid);
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
				assertEquals(pair.getValue(), expectedOutput);
				expDateRead = true;
			}
			else if (pair.getName().equals("expirationDateMonth")) {
				expectedOutput = JodaUtils.format(time, "MM");
				assertEquals(pair.getValue(), expectedOutput);
				expMonthRead = true;

			}
			else if (pair.getName().equals("expirationDateYear")) {
				expectedOutput = JodaUtils.format(time, "yyyy");
				assertEquals(expectedOutput, pair.getValue());
				expYearRead = true;
			}
		}
	}

}
