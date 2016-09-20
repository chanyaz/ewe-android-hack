package com.expedia.bookings.test.phone.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class FlightAustraliaAirlineFeeTest extends PhoneTestCase {

	//Test for maintaining Australian regulatory changes. d/5810
	public void testAustralianRegulatoryComplianceLabelsPresent() throws Exception {
		Common.setPOS(PointOfSaleId.AUSTRALIA);
		NewLaunchScreen.flightLaunchButton().perform(waitForViewToDisplay(), click());
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
		EspressoUtils.assertViewWithTextIsDisplayed("Airline fee applies based on payment method");
		FlightLegScreen.clickSelectFlightButton();
		EspressoUtils.assertViewWithTextIsDisplayed(
			R.id.airline_fee_bar, "Airlines charge an additional fee based on payment method.");
		EspressoUtils.assertViewWithTextIsDisplayed(
			R.id.flight_price_label_text_view, "Prices roundtrip, per person, from");
		FlightsSearchResultsScreen.clickListItem(1);
		EspressoUtils.assertViewWithSubstringIsDisplayed(R.id.right_text_view, "from");
		EspressoUtils.assertViewWithTextIsDisplayed("Airline fee applies based on payment method");
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
		// Of two similar TextViews, make sure only one is visible at once as
		// the user backs up and retraces their steps.
		Common.pressBack();
		EspressoUtils.assertViewIsDisplayed(R.id.airline_fee_notice);
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_notice_fee_added);
		CommonCheckoutScreen.clickCheckoutButton();
		EspressoUtils.assertViewIsDisplayed(R.id.airline_notice_fee_added);
		EspressoUtils.assertViewIsNotDisplayed(R.id.airline_fee_notice);
	}
}
