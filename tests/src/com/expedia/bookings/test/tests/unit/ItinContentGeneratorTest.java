package com.expedia.bookings.test.tests.unit;

import org.joda.time.DateTime;
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

	static final DateTime mNow = DateTime.now();
	DateTime mCheckInDate;
	DateTime mCheckOutDate;
	HotelItinContentGenerator mItinGen;
	TripHotel mTripHotel = new TripHotel();

	public void setUp() {
	}

	private ItinContentGenerator<?> getUpdatedItinGenerator(DateTime checkIn, DateTime checkOut, TripHotel trip) {
		trip.setStartDate(checkIn);
		trip.setEndDate(checkOut);
		return (HotelItinContentGenerator) ItinContentGenerator.createGenerator(getContext(), new ItinCardDataHotel(
				trip));
	}

	public void testHotelHeaderTextYesterday() {
		mCheckInDate = mNow.minusDays(1);
		mCheckOutDate = mNow.plusDays(5);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate,
				mCheckOutDate, mTripHotel);
		String headerText = mItinGen.getHeaderTextDate();
		String yesterday = getContext().getString(R.string.yesterday);
		assertEquals(yesterday, headerText);
	}

	public void testHotelHeaderTextToday() {
		mCheckInDate = mNow;
		mCheckOutDate = mNow.plusDays(5);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate,
				mCheckOutDate, mTripHotel);
		String headerText = mItinGen.getHeaderTextDate();
		String today = getContext().getString(R.string.Today);
		assertEquals(today, headerText);
	}

	public void testHotelHeaderTextTomorrow() {
		mCheckInDate = mNow.plusDays(1);
		mCheckOutDate = mNow.plusDays(5);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate, mCheckOutDate, mTripHotel);
		String headerText = mItinGen.getHeaderTextDate();
		String tomorrow = getContext().getString(R.string.tomorrow);
		assertEquals(tomorrow, headerText);
	}

	public void testHotelHeaderTextFuture30s() {
		mCheckInDate = mNow.plusSeconds(30);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate, mCheckOutDate, mTripHotel);
		String result = mItinGen.getHeaderTextDate();
		String expected = getContext().getString(R.string.Today);
		assertEquals(expected, result);
	}

	public void testHotelHeaderTextFuture119s() {
		mCheckInDate = mNow.plusSeconds(119);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate, mCheckOutDate, mTripHotel);
		String result = mItinGen.getHeaderTextDate();
		String expected = getContext().getString(R.string.Today);
		assertEquals(expected, result);
	}

	public void testHotelHeaderTextFuture2m() {
		mCheckInDate = mNow.plusMinutes(2);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate, mCheckOutDate, mTripHotel);
		String result = mItinGen.getHeaderTextDate();
		String expected = getContext().getString(R.string.Today);
		assertEquals(expected, result);
	}

	public void testHotelHeaderTextFuture2d() {
		mCheckInDate = mNow.plusDays(2);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate, mCheckOutDate, mTripHotel);
		String result = mItinGen.getHeaderTextDate();
		String expected = getContext().getResources().getQuantityString(R.plurals.days_from_now, 2, 2);
		assertEquals(expected, result);
	}

	public void testHotelHeaderTextFuture4d() {
		mCheckInDate = mNow.plusDays(4);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate, mCheckOutDate, mTripHotel);
		String headerText = mItinGen.getHeaderTextDate();
		String dateString = JodaUtils.formatDateTime(getContext(), mCheckInDate, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH);
		assertEquals(dateString, headerText);
	}

	private static final int MILLIS_IN_HOUR = 3600000;
	private static final DateTimeZone mDTZDefault = DateTimeZone.getDefault();

	public void testHotelHeaderTextTomorrowNextTimeZone() {
		//Today at 23:59:59
		mCheckInDate = mNow.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate, mCheckOutDate, mTripHotel);
		String headerText = mItinGen.getHeaderTextDate();
		String dateString = getContext().getString(R.string.Today);
		assertEquals(dateString, headerText);

		// Make a DateTimeZone that is +1 closer to UTC than default (local)
		// and update itinGenerator
		DateTimeZone dtz = DateTimeZone.forOffsetMillis(mDTZDefault.getOffset(mCheckInDate.getMillis())
				+ MILLIS_IN_HOUR);
		mCheckInDate = mCheckInDate.withZone(dtz);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate, mCheckOutDate,
				mTripHotel);

		headerText = mItinGen.getHeaderTextDate();
		dateString = getContext().getString(R.string.tomorrow);
		assertEquals(dateString, headerText);
	}

	public void testHotelHeaderTextYesterdayPreviousTimeZone() {
		//Today at 00:00:01
		mCheckInDate = mNow.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(1);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate, mCheckOutDate, mTripHotel);
		String headerText = mItinGen.getHeaderTextDate();
		String dateString = getContext().getString(R.string.Today);
		assertEquals(dateString, headerText);

		// Make a DateTimeZone that is 1 hour farther from UTC than default (local)
		// and update itinGenerator
		DateTimeZone dtz = DateTimeZone.forOffsetMillis(mDTZDefault.getOffset(mCheckInDate.getMillis())
				- MILLIS_IN_HOUR);
		mCheckInDate = mCheckInDate.withZone(dtz);
		mItinGen = (HotelItinContentGenerator) getUpdatedItinGenerator(mCheckInDate, mCheckOutDate,
				mTripHotel);

		headerText = mItinGen.getHeaderTextDate();
		dateString = getContext().getString(R.string.yesterday);
		assertEquals(dateString, headerText);
	}
}
