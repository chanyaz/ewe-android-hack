package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.test.robolectric.RobolectricRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class ItineraryManagerTest {

	@Test
	public void testHasUpcomingOrInProgressTrip() {
		DateTime now = LocalDateTime.now().toDateTime();
		DateTimeUtils.setCurrentMillisFixed(now.getMillis());
		List<DateTime> startDates = new ArrayList<>();
		startDates.add(now.minusDays(3));
		List<DateTime> endDates = new ArrayList<>();
		endDates.add(now.plusDays(12));
		assertEquals(true, ItineraryManager.hasUpcomingOrInProgressTrip(startDates, endDates));

		startDates.clear();
		endDates.clear();

		startDates.add(now.plusDays(12));
		endDates.add(now.plusDays(19));
		assertEquals(false, ItineraryManager.hasUpcomingOrInProgressTrip(startDates, endDates));

		startDates.clear();
		endDates.clear();

		startDates.add(now.plusDays(7));
		endDates.add(now.plusDays(10));
		assertEquals(false, ItineraryManager.hasUpcomingOrInProgressTrip(startDates, endDates));
	}

}
