package com.expedia.bookings.test.phone.hotels;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class HotelSearchPresenterTest extends HotelTestCase {

	public void testNoSearchUntilDateAndLocationSelected() throws Throwable {
		// search button disabled upon entry. Enter location.
		HotelScreen.searchButton().perform(click());
		HotelScreen.searchButton().check(matches(isDisplayed()));
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		//Search button will still be disabled
		HotelScreen.searchButton().perform(click());
		HotelScreen.searchButton().check(matches(isDisplayed()));
		// Open calendar and select dates
		HotelScreen.selectDateButton().check(matches(withText(R.string.select_dates)));
		LocalDate startDate = LocalDate.now().plusDays(35);
		HotelScreen.selectDates(startDate, null);
		//Search button will be enabled
		HotelScreen.searchButton().perform(click());
		HotelScreen.waitForResultsLoaded();
	}

	public void testChildAgeLabel() throws Throwable {
		// Select location
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		//Open guest picker
		HotelScreen.guestPicker().perform(click());
		HotelScreen.guestPicker().check(matches(withText("1 Guest")));
		HotelScreen.adultPicker().check(matches(withText("1 Adult")));
		//check label under child traveler selection
		HotelScreen.childPicker().check(matches(withText("0 Children")));
		HotelScreen.childAgeLabel().check(matches(withText("(0-17 years old)")));
	}

	public void testDateButtonTextPopulation() throws Throwable {
		// Select location
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
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
		String expected = expectedStartDate + " - " + expectedEndDate + "\n(5 nights)" ;
		HotelScreen.selectDateButton().check(matches(withText(expected)));
	}

	public void testSearchPresenterState() throws Throwable {
		// TODO : activity is removed on back press.
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		HotelScreen.assertCalendarShown();
	}
}
