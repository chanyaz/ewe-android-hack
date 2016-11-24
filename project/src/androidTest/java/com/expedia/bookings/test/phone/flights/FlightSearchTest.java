package com.expedia.bookings.test.phone.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.FlightTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen;
import com.mobiata.android.Log;

import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Created by dmadan on 5/2/14.
 */
public class FlightSearchTest extends FlightTestCase {
	/*
	*  #289 eb_tp test for flight search.
	*/
	private static final String TAG = "FlightSearchTest";

	// Test to check duplicate airport search gives error message
	public void testDuplicateAirportSearchGivesErrorMessage() throws Exception {
		FlightsSearchScreen.clickDepartureAirportField();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.clickArrivalAirportField();
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickSearchButton();
		EspressoUtils.assertViewWithTextIsDisplayed("Departure and arrival airports must be different.");
		Common.clickOkString();
	}

	//Test number of traveler shows correctly in Textview
	public void testGuestButtonTextView() throws Exception {
		FlightsSearchScreen.clickPassengerSelectionButton();
		for (int i = 1; i <= 6; i++) {
			String adultQuantity = FlightsSearchScreen.getTravelerNumberText();
			String adultQuantityTextView = EspressoUtils.getText(R.id.refinement_info_text_view);
			assertEquals(adultQuantity, adultQuantityTextView);
			FlightsSearchScreen.incrementAdultsButton();
		}
	}

	//Test clearing selected dates work
	public void testClearingSelectedDates() throws Exception {
		FlightsSearchScreen.clickDepartureAirportField();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.clickArrivalAirportField();
		FlightsSearchScreen.enterArrivalAirport("LAX");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickClearSelectedDatesButton();
		FlightsSearchScreen.checkHint("Select a departure date");
	}

	//Test can't search until all data is added.
	public void testTryingToSearchWithMissingInfo() {
		FlightsSearchScreen.enterDepartureAirport("SFO");
		try {
			FlightsSearchScreen.clickSearchButton();
		}
		catch (Exception e) {
			Log.v(TAG, "Wasn't able to click the search button without arrival, as expected.");
		}
		FlightsSearchScreen.enterArrivalAirport("LAX");
		FlightsSearchScreen.clickSelectDepartureButton();
		try {
			FlightsSearchScreen.clickSearchButton();
		}
		catch (Exception e) {
			Log.v(TAG, "Wasn't able to click the search button without date, as expected.");
		}
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.searchButton().check(matches(isDisplayed()));
	}

	//Test One way international flights,and getting to search results screen
	public void testOneWayInternationalFlight() throws Exception {
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("Frankfurt, Germany");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickSearchButton();
	}

	//Test Round trip international flights,and getting to search results screen
	public void testRoundTripInternationalFlight() throws Exception {
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("Frankfurt, Germany");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
	}

	//Test One way Domestic flights,and getting to search results screen
	public void testOneWayDomesticFlight() throws Exception {
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickSearchButton();
	}

	//Test Round trip international flights,and getting to search results screen
	public void testRoundTripDomesticFlight() throws Exception {
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
	}

	//Test for starting the same search again
	public void testSameSearch() throws Exception {
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		pressBack();
		FlightsSearchScreen.clickSearchButton();
	}

	//Test for locations with no airport
	public void testLocationwithNoAirport() throws Exception {
		FlightsSearchScreen.enterDepartureAirport("Meeker");
		FlightsSearchScreen.enterArrivalAirport("Colorado");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
	}

	//Ensure that Australian regulatory labels are NOT present for other POS. d/5810
	public void testAustralianRegulatoryComplianceOnlyForAustralia() throws Exception {
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_fee_bar);
		FlightsSearchResultsScreen.clickListItem(1);
		EspressoUtils.assertViewIsNotDisplayed(R.id.fees_secondary_text_view);
		FlightLegScreen.clickSelectFlightButton();
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_fee_bar);
		FlightsSearchResultsScreen.clickListItem(1);
		EspressoUtils.assertViewIsNotDisplayed(R.id.fees_secondary_text_view);
		FlightLegScreen.clickSelectFlightButton();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.total_price_label, "Trip Total");
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_fee_notice);
		FlightLegScreen.clickCostBreakdownButtonView();
		EspressoUtils.viewHasDescendantsWithText(R.id.breakdown_container, "Trip Total");
		FlightLegScreen.clickCostBreakdownDoneButton();
		CommonCheckoutScreen.clickCheckoutButton();
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_notice_fee_added);
	}

	public void testSWPEarnMessaging() throws Exception {
		FlightsSearchScreen.clickDepartureAirportField();
		FlightsSearchScreen.enterDepartureAirport("earn");
		FlightsSearchScreen.clickArrivalAirportField();
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickSearchButton();
		EspressoUtils.assertViewWithTextIsDisplayed("Earn 20 points");
		EspressoUtils.assertViewWithTextIsDisplayed("Earn $6.39");
		FlightsSearchResultsScreen.clickListItem(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.bottom_text_view, "Earn 20 points");
		FlightLegScreen.clickSelectFlightButton();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.earn_message, "Earn 20 points");
	}
}
