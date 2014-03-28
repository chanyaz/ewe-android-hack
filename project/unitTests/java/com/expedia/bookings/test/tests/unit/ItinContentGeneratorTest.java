package com.expedia.bookings.test.tests.unit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;

import android.content.res.Resources;
import android.test.AndroidTestCase;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.widget.itin.HotelItinContentGenerator;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.Log;

public class ItinContentGeneratorTest extends AndroidTestCase {

	DateTime mTodayAtNoon;

	public void setUp() {
		mTodayAtNoon = DateTime.now().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0);
	}

	private ItinContentGenerator<?> getItinGenerator(DateTime checkIn, DateTime checkOut) {
		TripHotel trip = new TripHotel();
		trip.setStartDate(checkIn);
		trip.setEndDate(checkOut);
		return (HotelItinContentGenerator) ItinContentGenerator.createGenerator(getContext(), new ItinCardDataHotel(
				trip));
	}

	private String getHeaderTextDate(DateTime checkInDate, DateTime checkOutDate) {
		ItinContentGenerator<?> itin = (HotelItinContentGenerator) getItinGenerator(checkInDate, checkOutDate);
		return itin.getHeaderTextDate();
	}

	public void testHotelHeaderTextYesterday() {
		DateTime ci = mTodayAtNoon.minusDays(1);
		DateTime co = mTodayAtNoon.plusDays(5);
		String headerText = getHeaderTextDate(ci, co);
		String yesterday = getContext().getString(R.string.yesterday);
		assertEquals(yesterday, headerText);
	}

	public void testHotelHeaderTextToday() {
		DateTime ci = mTodayAtNoon;
		DateTime co = mTodayAtNoon.plusDays(5);
		String headerText = getHeaderTextDate(ci, co);
		String today = getContext().getString(R.string.Today);
		assertEquals(today, headerText);
	}

	public void testHotelHeaderTextTomorrow() {
		DateTime ci = mTodayAtNoon.plusDays(1);
		DateTime co = mTodayAtNoon.plusDays(5);
		String headerText = getHeaderTextDate(ci, co);
		String tomorrow = getContext().getString(R.string.tomorrow);
		assertEquals(tomorrow, headerText);
	}

	public void testHotelHeaderTextFuture30s() {
		DateTime ci = mTodayAtNoon.plusSeconds(30);
		DateTime co = mTodayAtNoon.plusDays(7);
		String result = getHeaderTextDate(ci, co);
		String expected = getContext().getString(R.string.Today);
		assertEquals(expected, result);
	}

	public void testHotelHeaderTextFuture119s() {
		DateTime ci = mTodayAtNoon.plusSeconds(119);
		DateTime co = mTodayAtNoon.plusDays(7);
		String result = getHeaderTextDate(ci, co);
		String expected = getContext().getString(R.string.Today);
		assertEquals(expected, result);
	}

	public void testHotelHeaderTextFuture2m() {
		DateTime ci = mTodayAtNoon.plusMinutes(2);
		DateTime co = mTodayAtNoon.plusDays(7);
		String result = getHeaderTextDate(ci, co);
		String expected = getContext().getString(R.string.Today);
		assertEquals(expected, result);
	}

	public void testHotelHeaderTextFuture2d() {
		DateTime ci = mTodayAtNoon.plusDays(2);
		DateTime co = mTodayAtNoon.plusDays(10);
		String result = getHeaderTextDate(ci, co);
		String expected = getContext().getResources().getQuantityString(R.plurals.days_from_now, 2, 2);
		assertEquals(expected, result);
	}

	public void testHotelHeaderTextFuture4d() {
		DateTime ci = mTodayAtNoon.plusDays(4);
		DateTime co = mTodayAtNoon.plusDays(10);
		String headerText = getHeaderTextDate(ci, co);
		String dateString = JodaUtils.formatDateTime(getContext(), ci, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH);
		assertEquals(dateString, headerText);
	}

	private static final int MILLIS_IN_HOUR = 3600000;
	private static final DateTimeZone mDTZDefault = DateTimeZone.getDefault();

	public void testHotelHeaderTextTomorrowNextTimeZone() {
		DateTime co = mTodayAtNoon.plusDays(10);

		//Today at 23:59:59
		DateTime ci = mTodayAtNoon.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
		String headerText = getHeaderTextDate(ci, co);
		String dateString = getContext().getString(R.string.Today);
		assertEquals(dateString, headerText);

		// Make a DateTimeZone that is +1 closer to UTC than default (local)
		// and update itinGenerator
		DateTimeZone dtz = DateTimeZone.forOffsetMillis(mDTZDefault.getOffset(ci.getMillis())
				+ MILLIS_IN_HOUR);
		ci = ci.withZone(dtz);

		headerText = getHeaderTextDate(ci, co);
		dateString = getContext().getString(R.string.tomorrow);
		assertEquals(dateString, headerText);
	}

	public void testHotelHeaderTextYesterdayPreviousTimeZone() {
		DateTime co = mTodayAtNoon.plusDays(10);

		//Today at 00:00:01
		DateTime ci = mTodayAtNoon.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(1);
		String headerText = getHeaderTextDate(ci, co);
		String dateString = getContext().getString(R.string.Today);
		assertEquals(dateString, headerText);

		// Make a DateTimeZone that is 1 hour farther from UTC than default (local)
		// and update itinGenerator
		DateTimeZone dtz = DateTimeZone.forOffsetMillis(mDTZDefault.getOffset(ci.getMillis())
				- MILLIS_IN_HOUR);
		ci = ci.withZone(dtz);

		headerText = getHeaderTextDate(ci, co);
		dateString = getContext().getString(R.string.yesterday);
		assertEquals(dateString, headerText);
	}

	public void testHotelHeaderTextAtFixedTimes() {
		final DateTime elevenFifty = DateTime.now(DateTimeZone.UTC).withHourOfDay(23).withMinuteOfHour(50);
		DateTimeUtils.setCurrentMillisFixed(elevenFifty.getMillis());

		// Run tests
		testHotelHeaderTextTomorrowNextTimeZone();
		testHotelHeaderTextYesterdayPreviousTimeZone();

		// Reset JodaTime to system clock
		DateTimeUtils.setCurrentMillisSystem();
	}
}
