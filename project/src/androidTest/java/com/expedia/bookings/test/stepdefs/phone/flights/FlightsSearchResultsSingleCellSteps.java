package com.expedia.bookings.test.stepdefs.phone.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.phone.newflights.FlightsResultsScreen;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;


public class FlightsSearchResultsSingleCellSteps {

	@And("^Validate that flight time field at cell (\\d+) is displayed: (true|false) and isOutBound : (true|false)$")
	public void checkVisibilityFlightDuration(int cellNumber,boolean isDisplayed, boolean outbound) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(cellNumber,R.id.flight_time_detail_text_view, isDisplayed,outbound);
	}

	@And("^Validate that price field at cell (\\d+) is displayed: (true|false) and isOutBound : (true|false)$")
	public void checkVisibilityOfPrice(int cellNumber,boolean isDisplayed, boolean outbound) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(cellNumber,R.id.price_text_view, isDisplayed,outbound);
	}

	@And("^Validate that airline name field at cell (\\d+) is displayed: (true|false) and isOutBound : (true|false)$")
	public void checkVisibilityOfAirlineName(int cellNumber,boolean isDisplayed, boolean outbound) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(cellNumber,R.id.airline_text_view, isDisplayed,outbound);
	}

	@And("^Validate that flight duration field at cell (\\d+) is displayed: (true|false) and isOutBound : (true|false)$")
	public void checkVisibilityOfFlightDuration(int cellNumber,boolean isDisplayed, boolean outbound) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(cellNumber,R.id.flight_duration_text_view, isDisplayed,outbound);
	}

	@And("^Validate that round trip header at cell (\\d+) is displayed: (true|false) and isOutBound : (true|false)$")
	public void checkVisibilityOfRoundTripHeader(int cellNumber,boolean isDisplayed, boolean outbound) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(cellNumber,R.id.trip_type_text_view, isDisplayed,outbound);
	}

	private void validateFlightSRPListViewCellItemVisibility(int cellNumber,int resId, boolean isDisplayed,boolean outBound) {
		onView(allOf(withId(R.id.list_view),(outBound ? isDescendantOfA(withId(R.id.widget_flight_outbound)) : isDescendantOfA(withId(R.id.widget_flight_inbound))))).check(RecyclerViewAssertions.assertionOnItemAtPosition(cellNumber, hasDescendant(
			allOf(withId(resId), (isDisplayed ? isDisplayed() : not(isDisplayed()))))));
	}

	private void checkString(int cellNumber,int resID, String text,boolean outBound) {
		onView(allOf(withId(R.id.list_view),(outBound ? isDescendantOfA(withId(R.id.widget_flight_outbound)) : isDescendantOfA(withId(R.id.widget_flight_inbound))))).check(RecyclerViewAssertions.assertionOnItemAtPosition(cellNumber, hasDescendant(
			allOf(withId(resID), withText(containsString(text))))));
	}

	@And("^Name of airline at cell (\\d+) is \"(.*?)\" and isOutBound : (true|false)$")
	public void checkAirlineName(int cellNumber,String airline, boolean outbound) throws Throwable {
		checkString(cellNumber,R.id.airline_text_view, airline,outbound);
	}

	@And("^Price of the flight at cell (\\d+) is (\\d+) and isOutBound : (true|false)$")
	public void checkPriceOfFlight(int cellNumber,int price, boolean outbound) throws Throwable {
		checkString(cellNumber,R.id.price_text_view, String.valueOf(price),outbound);
	}

	@And("^Duration of the flight at cell (\\d+) is \"(.*?)\" and isOutBound : (true|false)$")
	public void checkDuraionOfFlight(int cellNumber,String duration, boolean outbound) throws Throwable {
		checkString(cellNumber,R.id.flight_duration_text_view, duration,outbound);
	}

	@And("^Timing of the flight at cell (\\d+) is \"(.*?)\" and isOutBound : (true|false)$")
	public void checkTimingOfTheFlight(int cellNumber,String timing, boolean outbound) throws Throwable {
		checkString(cellNumber,R.id.flight_time_detail_text_view, timing,outbound);
	}

	@And("^Number of stops at cell (\\d+) are (\\d+) and isOutBound : (true|false)$")
	public void numberOfStops(int cellNumber,int stops, boolean outbound) throws Throwable {
		if (stops > 0) {
			checkString(cellNumber,R.id.flight_duration_text_view, (String.valueOf(stops) + " Stop"),outbound);
		}
		else {
			checkString(cellNumber,R.id.flight_duration_text_view, "Nonstop",outbound);
		}
	}

	@And("^the currency symbol at cell (\\d+) on FSR is \"(.*?)\" and isOutBound : (true|false)$")
	public void checkCurrencyOnFSR(int cellNumber,String currencySymbol, boolean outbound) throws Throwable {
		checkString(cellNumber,R.id.price_text_view, currencySymbol,outbound);
	}

	@Then("^Validate that on the selected outbound docked view Flight label is displayed$")
	public void validateOutboundFlightLabel() throws Throwable {
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("Outbound");
	}

	@And("^Validate that on the selected outbound docked view Flight Airline name is displayed$")
	public void validateOutboundFlightAirlinename() throws Throwable {
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("happy_round_trip");
	}

	@And("Validate that on the selected outbound docked view Airline time is displayed$")
	public void validateOutboundFlightAirlinetime() throws Throwable {
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("9:00 pm - 11:00 pm (2h 0m)");
	}

	@And("^Validate the toolbar header text on the selected outbound docked view$")
	public void validatetoolbarheadertext() throws Throwable {
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText("Select return flight")))
			.check(matches(isDisplayed()));
	}

	@Then("^urgency message on cell (\\d+) isDisplayed : (true|false)$")
	public void lookForUrgencyMessage(int cellNumber, boolean isDisplayed) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(cellNumber,R.id.urgency_message,isDisplayed,true);
	}
}

