package com.expedia.bookings.widget.itin;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.itin.data.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.utils.JodaUtils;
import com.squareup.phrase.Phrase;

@RunWith(RobolectricRunner.class)
public class ItinContentGeneratorTest {

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	DateTime mTodayAtNoon;

	@Before
	public void before() {
		mTodayAtNoon = DateTime.now().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0);
	}

	private ItinContentGenerator<?> getItinGenerator(DateTime checkIn, DateTime checkOut) {
		TripHotel trip = new TripHotel();
		Property property = new Property();
		property.setPropertyId("1");
		trip.setProperty(property);
		trip.setStartDate(checkIn);
		trip.setEndDate(checkOut);
		return ItinContentGenerator.createGenerator(getContext(), new ItinCardDataHotel(trip));
	}

	private String getHeaderTextDate(DateTime checkInDate, DateTime checkOutDate) {
		ItinContentGenerator<?> itin = getItinGenerator(checkInDate, checkOutDate);
		return itin.getHeaderTextDate();
	}

	@Test
	public void testHotelHeaderTextYesterday() {
		DateTime ci = mTodayAtNoon.minusDays(1);
		DateTime co = mTodayAtNoon.plusDays(5);
		String headerText = getHeaderTextDate(ci, co);
		String yesterday = getContext().getString(R.string.yesterday);
		Assert.assertEquals(yesterday, headerText);
	}

	@Test
	public void testHotelHeaderTextToday() {
		DateTime ci = mTodayAtNoon;
		DateTime co = mTodayAtNoon.plusDays(5);
		String headerText = getHeaderTextDate(ci, co);
		String today = getContext().getString(R.string.Today);
		Assert.assertEquals(today, headerText);
	}

	@Test
	public void testHotelHeaderTextTomorrow() {
		DateTime ci = mTodayAtNoon.plusDays(1);
		DateTime co = mTodayAtNoon.plusDays(5);
		String headerText = getHeaderTextDate(ci, co);
		String tomorrow = getContext().getString(R.string.tomorrow);
		Assert.assertEquals(tomorrow, headerText);
	}

	@Test
	public void testHotelHeaderTextFuture30s() {
		DateTime ci = mTodayAtNoon.plusSeconds(30);
		DateTime co = mTodayAtNoon.plusDays(7);
		String result = getHeaderTextDate(ci, co);
		String expected = getContext().getString(R.string.Today);
		Assert.assertEquals(expected, result);
	}

	@Test
	public void testHotelHeaderTextFuture119s() {
		DateTime ci = mTodayAtNoon.plusSeconds(119);
		DateTime co = mTodayAtNoon.plusDays(7);
		String result = getHeaderTextDate(ci, co);
		String expected = getContext().getString(R.string.Today);
		Assert.assertEquals(expected, result);
	}

	@Test
	public void testHotelHeaderTextFuture2m() {
		DateTime ci = mTodayAtNoon.plusMinutes(2);
		DateTime co = mTodayAtNoon.plusDays(7);
		String result = getHeaderTextDate(ci, co);
		String expected = getContext().getString(R.string.Today);
		Assert.assertEquals(expected, result);
	}

	@Test
	public void testHotelHeaderTextFuture2d() {
		DateTime ci = mTodayAtNoon.plusDays(2);
		DateTime co = mTodayAtNoon.plusDays(10);
		String result = getHeaderTextDate(ci, co);
		String expected = Phrase
			.from(getContext().getResources().getQuantityString(R.plurals.days_from_now, 2))
			.put("days", 2).format().toString();
		Assert.assertEquals(expected, result);
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
		MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS})
	public void testHotelHeaderTextFuture4d() {
		DateTime ci = mTodayAtNoon.plusDays(4);
		DateTime co = mTodayAtNoon.plusDays(10);
		String headerText = getHeaderTextDate(ci, co);
		String dateString = JodaUtils.formatDateTime(getContext(), ci, DateUtils.FORMAT_SHOW_DATE
			| DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH);
		Assert.assertEquals(dateString, headerText);
	}

	private static final int MILLIS_IN_HOUR = 3600000;
	private static final DateTimeZone mDTZDefault = DateTimeZone.getDefault();

	@Test
	public void testHotelHeaderTextTomorrowNextTimeZone() {
		DateTime co = mTodayAtNoon.plusDays(10);

		//Today at 23:59:59
		DateTime ci = DateTime.now().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(0);
		String headerText = getHeaderTextDate(ci, co);
		String dateString = getContext().getString(R.string.Today);
		Assert.assertEquals(dateString, headerText);

		// Make a DateTimeZone that is +1 closer to UTC than default (local)
		// and update itinGenerator
		DateTimeZone dtz = DateTimeZone.forOffsetMillis(
			mDTZDefault.getOffset(ci.getMillis()) + MILLIS_IN_HOUR);
		ci = ci.withZone(dtz);

		headerText = getHeaderTextDate(ci, co);
		dateString = getContext().getString(R.string.tomorrow);
		Assert.assertEquals(dateString, headerText);
	}

	@Test
	public void testHotelHeaderTextYesterdayPreviousTimeZone() {
		DateTime co = mTodayAtNoon.plusDays(10);

		//Today at 00:00:01
		DateTime ci = DateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(1);
		String headerText = getHeaderTextDate(ci, co);
		String dateString = getContext().getString(R.string.Today);
		Assert.assertEquals(dateString, headerText);

		// Make a DateTimeZone that is 1 hour farther from UTC than default (local)
		// and update itinGenerator
		DateTimeZone dtz = DateTimeZone.forOffsetMillis(mDTZDefault.getOffset(ci.getMillis())
			- MILLIS_IN_HOUR);
		ci = ci.withZone(dtz);

		headerText = getHeaderTextDate(ci, co);
		dateString = getContext().getString(R.string.yesterday);
		Assert.assertEquals(dateString, headerText);
	}

	@Test
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
