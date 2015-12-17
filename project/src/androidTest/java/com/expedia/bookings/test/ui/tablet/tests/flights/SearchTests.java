package com.expedia.bookings.test.ui.tablet.tests.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

/**
 * Created by dmadan on 8/8/14.
 */
public class SearchTests extends TabletTestCase {

	/**
	 * Run these search regression tests on Integration/Production api servers only
	 */

	public void testNoSearchesLongerThan28Days() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Search.clickSuggestion("San Francisco, CA");
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(30);
		Search.clickSelectFlightDates();
		Search.clickDate(startDate, endDate);
		Search.clickSearchPopupDone();
		EspressoUtils.assertViewWithTextIsDisplayed("We're sorry, but we are unable to search for hotel stays longer than 28 days.");
	}

	public void testThat28DaySearchesWork() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Search.clickSuggestion("San Francisco, CA");
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(28);
		Search.clickSelectFlightDates();
		Search.clickDate(startDate, endDate);
		Search.clickSearchPopupDone();
		Results.swipeUpHotelList();
	}

	public void testGeocodeResolutionOfCityAbbreviations() throws Exception {
		String cityAbbreviation = "NYC ";
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText(cityAbbreviation);
		Launch.clickSuggestion("New York, NY");

		Launch.clickDestinationSearchButton();
		cityAbbreviation = "SF ";
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText(cityAbbreviation);
		Launch.clickSuggestion("San Francisco, CA");
	}

	public void testPointOfInterestGeocoding() throws Exception {
		String pointOfInterest = "Statue of Liberty ";
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText(pointOfInterest);
		Launch.clickSuggestion("Statue of Liberty, New York, NY");
	}
}
