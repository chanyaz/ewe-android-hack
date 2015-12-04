package com.expedia.bookings.test.ui.phone.tests.flights;

import org.joda.time.LocalDate;

import android.support.test.espresso.assertion.ViewAssertions;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.expedia.bookings.test.ui.tablet.pagemodels.Common.pressBack;

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
		FlightsSearchScreen.searchButton().check(ViewAssertions.matches(isDisplayed()));
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

	//Test for maintaining Australian regulatory changes. d/5810
	public void testAustralianRegulatoryComplianceLabelsPresent() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		setPOS(PointOfSaleId.AUSTRALIA);
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		EspressoUtils.assertViewWithTextIsDisplayed(
				R.id.airline_fee_bar, "Airlines charge an additional fee based on payment method.");
		EspressoUtils.assertViewWithTextIsDisplayed(
				R.id.flight_price_label_text_view, "Prices roundtrip, per person, from");
		FlightsSearchResultsScreen.clickListItem(1);
		EspressoUtils.assertViewWithSubstringIsDisplayed(R.id.right_text_view, "from");
		EspressoUtils.assertViewWithTextIsDisplayed(
				R.id.airline_fee_notice_payment, "Airline fee applies based on payment method.");
		FlightLegScreen.clickSelectFlightButton();
		EspressoUtils.assertViewWithTextIsDisplayed(
				R.id.airline_fee_bar, "Airlines charge an additional fee based on payment method.");
		EspressoUtils.assertViewWithTextIsDisplayed(
				R.id.flight_price_label_text_view, "Prices roundtrip, per person, from");
		FlightsSearchResultsScreen.clickListItem(1);
		EspressoUtils.assertViewWithSubstringIsDisplayed(R.id.right_text_view, "from");
		EspressoUtils.assertViewWithTextIsDisplayed(
				R.id.airline_fee_notice_payment, "Airline fee applies based on payment method.");
		FlightLegScreen.clickSelectFlightButton();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.total_price_label, "Trip Total From");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.taxes_fees_label, "Includes taxes");
		EspressoUtils.assertViewWithTextIsDisplayed(
				R.id.airline_fee_notice,
				"An airline fee, based on card type, is added upon payment.");
		FlightLegScreen.clickCostBreakdownButtonView();
		EspressoUtils.viewHasDescendantsWithText(R.id.breakdown_container, "Trip Total From");
		EspressoUtils.viewHasDescendantsWithText(R.id.breakdown_container, "Taxes");
		FlightLegScreen.clickCostBreakdownDoneButton();
		CommonCheckoutScreen.clickCheckoutButton();
		EspressoUtils.assertViewWithSubstringIsDisplayed(
				R.id.airline_notice_fee_added,
				"An airline fee, based on card type, is added upon payment.");
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Ensure that Australian regulatory labels are NOT present for other POS. d/5810
	public void testAustralianRegulatoryComplianceOnlyForAustralia() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_fee_bar);
		FlightsSearchResultsScreen.clickListItem(1);
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_fee_notice_payment);
		FlightLegScreen.clickSelectFlightButton();
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_fee_bar);
		FlightsSearchResultsScreen.clickListItem(1);
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_fee_notice_payment);
		FlightLegScreen.clickSelectFlightButton();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.total_price_label, "Trip Total");
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_fee_notice);
		FlightLegScreen.clickCostBreakdownButtonView();
		EspressoUtils.viewHasDescendantsWithText(R.id.breakdown_container, "Trip Total");
		FlightLegScreen.clickCostBreakdownDoneButton();
		CommonCheckoutScreen.clickCheckoutButton();
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_notice_fee_added);
		ScreenActions.enterLog(TAG, "END TEST");
	}
}
