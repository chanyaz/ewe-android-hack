package com.expedia.bookings.test.phone.newhotels;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class HotelSearchPresenterTest extends HotelTestCase {

	public void testDateButtonTextPopulation() throws Throwable {
		// Select location
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA");
		// Open calendar
		HotelScreen.selectDateButton().check(matches(withText(R.string.select_dates)));

		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);

		// Select start date
		HotelScreen.selectDates(startDate, null);
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		HotelScreen.selectDateButton().check(matches(withText(expectedStartDate + " - Select check out date")));

		// Select end date
		HotelScreen.selectDates(startDate, endDate);
		String expectedEndDate = DateUtils.localDateToMMMd(endDate);
		String expected = expectedStartDate + " - " + expectedEndDate + "(5 nights)" ;
		HotelScreen.selectDateButton().check(matches(withText(expected)));
	}

	public void testSearchPresenterState() throws Throwable {
		// TODO : activity is removed on back press.
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA");
		HotelScreen.showCalendar();
	}

}
