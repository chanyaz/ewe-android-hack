package com.expedia.bookings.widget.itin;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.test.robolectric.RobolectricRunner;

@RunWith(RobolectricRunner.class)
public class HotelItinContentGeneratorTest {

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	DateTime mTodayAtNoon;

	@Before
	public void before() {
		mTodayAtNoon = DateTime.now().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0);
	}

	private HotelItinContentGenerator getHotelItinGenerator(DateTime checkIn, DateTime checkOut) {
		TripHotel trip = new TripHotel();
		Property property = new Property();
		property.setPropertyId("1");
		trip.setProperty(property);
		trip.setStartDate(checkIn);
		trip.setEndDate(checkOut);
		return new HotelItinContentGenerator(getContext(), new ItinCardDataHotel(trip));
	}

	private String getSummaryText(DateTime checkInDate, DateTime checkOutDate) {
		ItinContentGenerator<?> itin = getHotelItinGenerator(checkInDate, checkOutDate);
		ItinCardDataHotel data = getHotelItinGenerator(checkInDate, checkOutDate).getItinCardData();
		return getHotelItinGenerator(checkOutDate, checkOutDate).getSummaryText(data);
	}

	@Test
	public void testDontShowCancelLinkPastCheckInDate() {
		DateTime oldCheckInDate = DateTime.now().minusHours(1);
		HotelItinContentGenerator hotelItinGenerator = getHotelItinGenerator(oldCheckInDate, null);
		Assert.assertTrue(hotelItinGenerator.getItinCardData().isPastCheckInDate());
	}

	@Test
	public void testSummaryCheckInFuture4d() {
		DateTime checkInTime = mTodayAtNoon.plusDays(4);
		DateTime checkOutTime = mTodayAtNoon.plusDays(5);
		ItinCardDataHotel data = getHotelItinGenerator(checkInTime, checkOutTime).getItinCardData();
		String text = getContext().getString(R.string.itin_card_hotel_summary_check_in_day_TEMPLATE,
			data.getFormattedDetailsCheckInDate(getContext()));
		String summaryText = getSummaryText(checkInTime, checkOutTime);
		Assert.assertEquals(summaryText, text);
	}

	@Test
	public void testSummaryCheckInFuture3d() {
		DateTime checkInTime = mTodayAtNoon.plusDays(3);
		DateTime checkOutTime = mTodayAtNoon.plusDays(5);
		String text = getContext().getString(R.string.itin_card_hotel_summary_check_in_three_days);
		String summaryText = getSummaryText(checkInTime, checkOutTime);
		Assert.assertEquals(summaryText, text);
	}

	@Test
	public void testSummaryCheckInFuture2d() {
		DateTime checkInTime = mTodayAtNoon.plusDays(2);
		DateTime checkOutTime = mTodayAtNoon.plusDays(5);
		String text = getContext().getString(R.string.itin_card_hotel_summary_check_in_two_days);
		String summaryText = getSummaryText(checkInTime, checkOutTime);
		Assert.assertEquals(summaryText, text);
	}

	@Test
	public void testSummaryCheckInFuture1d() {
		DateTime checkInTime = mTodayAtNoon.plusDays(1);
		DateTime checkOutTime = mTodayAtNoon.plusDays(5);
		String text = getContext().getString(R.string.itin_card_hotel_summary_check_in_tomorrow);
		String summaryText = getSummaryText(checkInTime, checkOutTime);
		Assert.assertEquals(summaryText, text);
	}

	@Test
	public void testSummaryCheckInToday() {
		DateTime checkInTime = mTodayAtNoon;
		DateTime checkOutTime = mTodayAtNoon.plusDays(5);
		ItinCardDataHotel data = getHotelItinGenerator(checkInTime, checkOutTime).getItinCardData();
		String text = getContext().getString(R.string.itin_card_hotel_summary_check_in_TEMPLATE,
			data.getFallbackCheckInTime(getContext()));
		String summaryText = getSummaryText(checkInTime, checkOutTime);
		Assert.assertEquals(summaryText, text);
	}

	@Test
	public void testSummaryCheckOutFuture4d() {
		DateTime checkInTime = mTodayAtNoon.minusDays(1);
		DateTime checkOutTime = mTodayAtNoon.plusDays(4);
		ItinCardDataHotel data = getHotelItinGenerator(checkInTime, checkOutTime).getItinCardData();
		String text = getContext().getString(R.string.itin_card_hotel_summary_check_out_day_TEMPLATE,
			data.getFormattedDetailsCheckOutDate(getContext()));
		String summaryText = getSummaryText(checkInTime, checkOutTime);
		Assert.assertEquals(summaryText, text);
	}

	@Test
	public void testSummaryCheckOutFuture3d() {
		DateTime checkInTime = mTodayAtNoon.minusDays(1);
		DateTime checkOutTime = mTodayAtNoon.plusDays(3);
		String text = getContext().getString(R.string.itin_card_hotel_summary_check_out_three_days);
		String summaryText = getSummaryText(checkInTime, checkOutTime);
		Assert.assertEquals(summaryText, text);
	}

	@Test
	public void testSummaryCheckOutFuture2d() {
		DateTime checkInTime = mTodayAtNoon.minusDays(1);
		DateTime checkOutTime = mTodayAtNoon.plusDays(2);
		String text = getContext().getString(R.string.itin_card_hotel_summary_check_out_two_days);
		String summaryText = getSummaryText(checkInTime, checkOutTime);
		Assert.assertEquals(summaryText, text);
	}

	@Test
	public void testSummaryCheckOutFuture1d() {
		DateTime checkInTime = mTodayAtNoon.minusDays(1);
		DateTime checkOutTime = mTodayAtNoon.plusDays(1);
		String text = getContext().getString(R.string.itin_card_hotel_summary_check_out_tomorrow);
		String summaryText = getSummaryText(checkInTime, checkOutTime);
		Assert.assertEquals(summaryText, text);
	}

	@Test
	public void testSummaryCheckOutToday() {
		DateTime checkInTime = mTodayAtNoon.minusDays(1);
		DateTime checkOutTime = mTodayAtNoon;
		ItinCardDataHotel data = getHotelItinGenerator(checkInTime, checkOutTime).getItinCardData();
		String text = (getContext().getString(R.string.itin_card_hotel_summary_check_out_TEMPLATE,
			data.getFallbackCheckOutTime(getContext())));
		String summaryText = getSummaryText(checkInTime, checkOutTime);
		Assert.assertEquals(summaryText, text);
	}

	@Test
	public void testSummaryCheckOutYesterday() {
		DateTime checkInTime = mTodayAtNoon.minusDays(3);
		DateTime checkOutTime = mTodayAtNoon.minusDays(1);
		ItinCardDataHotel data = getHotelItinGenerator(checkInTime, checkOutTime).getItinCardData();
		String text = getContext().getString(R.string.itin_card_hotel_summary_checked_out_day_TEMPLATE,
			data.getFormattedDetailsCheckOutDate(getContext()));
		String summaryText = getSummaryText(checkInTime, checkOutTime);
		Assert.assertEquals(summaryText, text);
	}

}
