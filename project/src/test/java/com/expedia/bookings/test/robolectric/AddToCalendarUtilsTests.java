package com.expedia.bookings.test.robolectric;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.text.TextUtils;

import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.utils.AddToCalendarUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricRunner.class)
public class AddToCalendarUtilsTests {

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void generateHotelAddToCalendarIntentCheckOutDate() {
		String confirmationNumber = "112358132134";
		testGenerateHotelAddToCalendarIntent(false, confirmationNumber, "");
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void generateHotelAddToCalendarIntentCheckInDate() {
		String confirmationNumber = "112358132134";
		testGenerateHotelAddToCalendarIntent(true, confirmationNumber, "");
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void generateHotelAddToCalendarIntentItineraryNumber() {
		String itineraryNumber = "431231853211";
		testGenerateHotelAddToCalendarIntent(false, "", itineraryNumber);
	}

	private void testGenerateHotelAddToCalendarIntent(boolean isCheckIn, String confirmationNumber, String itineraryNumber) {
		Property property = new Property();
		Location mockLocation = Mockito.mock(Location.class);
		String expectedLocation = "114 Sansome St";
		String expectedHotelName = "Bay Club";

		property.setName(expectedHotelName);
		property.setLocation(mockLocation);
		LocalDate date = new LocalDate().plusDays(1);

		Mockito.when(mockLocation.toLongFormattedString()).thenReturn(expectedLocation);
		Intent intent = AddToCalendarUtils.generateHotelAddToCalendarIntent(getContext(), property, date, isCheckIn, confirmationNumber, itineraryNumber);

		makeAddToCalendarIntentAssertions(intent, isCheckIn, expectedHotelName, expectedLocation, confirmationNumber, itineraryNumber,
			date);
	}

	public static void makeAddToCalendarIntentAssertions(Intent intent, boolean isCheckIn, String expectedHotelName, String expectedLocation,
		String confirmationNumber, String itineraryNumber, LocalDate date) {
		String expectedTitle = (isCheckIn ? "Check In: " : "Check Out: ") + expectedHotelName;

		assertEquals(expectedTitle, intent.getStringExtra(CalendarContract.Events.TITLE));
		assertEquals(date.toDateTimeAtStartOfDay().getMillis(), intent.getLongExtra(
			CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0));
		assertTrue(intent.getBooleanExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false));
		assertEquals(expectedLocation, intent.getStringExtra(CalendarContract.Events.EVENT_LOCATION));
		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(confirmationNumber)) {
			sb.append("Confirmation #: ");
			sb.append(confirmationNumber);
		}
		else {
			sb.append("Itinerary #: ");
			sb.append(itineraryNumber);
			sb.append("\n");
		}
		sb.append("\nIf you have questions about your reservation, please call Expedia at 1-877-222-6503.");
		assertEquals(sb.toString(), intent.getStringExtra(CalendarContract.Events.DESCRIPTION));
	}

}
