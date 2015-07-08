package com.expedia.bookings.test.ui.phone.tests.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Created by dmadan on 5/2/14.
 */
public class FlightSearchTests extends PhoneTestCase {
	/*
	*  #289 eb_tp test for flight search.
	*/
	private static final String TAG = "FlightSearchTests";

	// Test to check duplicate airport search gives error message
	public void testDuplicateAirportSearchGivesErrorMessage() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.clickDepartureAirportField();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.clickArrivalAirportField();
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickSearchButton();
		EspressoUtils.assertViewWithTextIsDisplayed("Departure and arrival airports must be different.");
		ScreenActions.enterLog(TAG, "Duplicate airport search error message displayed.");
		SettingsScreen.clickOkString();
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test number of traveler shows correctly in Textview
	public void testGuestButtonTextView() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.clickPassengerSelectionButton();
		for (int i = 1; i <= 6; i++) {
			String adultQuantity = FlightsSearchScreen.getTravelerNumberText();
			String adultQuantityTextView = EspressoUtils.getText(R.id.refinement_info_text_view);
			assertEquals(adultQuantity, adultQuantityTextView);
			FlightsSearchScreen.incrementAdultsButton();
		}
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test clearing selected dates work
	public void testClearingSelectedDates() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.clickDepartureAirportField();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.clickArrivalAirportField();
		FlightsSearchScreen.enterArrivalAirport("LAX");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickClearSelectedDatesButton();
		FlightsSearchScreen.checkHint("Select a departure date");
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test can't search until all data is added.
	public void testTryingToSearchWithMissingInfo() {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		try {
			FlightsSearchScreen.clickSearchButton();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Wasn't able to click the search button without arrival, as expected.");
		}
		FlightsSearchScreen.enterArrivalAirport("LAX");
		FlightsSearchScreen.clickSelectDepartureButton();
		try {
			FlightsSearchScreen.clickSearchButton();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Wasn't able to click the search button without date, as expected.");
		}
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.searchButton().check(matches(isDisplayed()));
		ScreenActions.enterLog(TAG, "Successfully asserted that the search button is shown.");
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test One way international flights,and getting to search results screen
	public void testOneWayInternationalFlight() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("Frankfurt, Germany");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickSearchButton();
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test Round trip international flights,and getting to search results screen
	public void testRoundTripInternationalFlight() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("Frankfurt, Germany");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test One way Domestic flights,and getting to search results screen
	public void testOneWayDomesticFlight() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickSearchButton();
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test Round trip international flights,and getting to search results screen
	public void testRoundTripDomesticFlight() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test for starting the same search again
	public void testSameSearch() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		pressBack();
		FlightsSearchScreen.clickSearchButton();
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test for locations with no airport
	public void testLocationwithNoAirport() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("Meeker");
		FlightsSearchScreen.enterArrivalAirport("Colorado");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		ScreenActions.enterLog(TAG, "END TEST");
	}

}
