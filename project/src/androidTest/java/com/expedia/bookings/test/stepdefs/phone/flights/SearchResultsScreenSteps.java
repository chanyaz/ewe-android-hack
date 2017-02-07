package com.expedia.bookings.test.stepdefs.phone.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class SearchResultsScreenSteps {
	@Then("^I select first flight$")
	public void selectFirstFlight() throws Throwable {
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0);
		FlightsScreen.selectOutboundFlight().perform(click());
	}

	@And("^Validate that flight time field is displayed: (true|false)$")
	public void checkVisibilityFlightDuration(boolean isDisplayed) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(R.id.flight_time_detail_text_view, isDisplayed);
	}

	@And("^Validate that price field is displayed: (true|false)$")
	public void checkVisibilityOfPrice(boolean isDisplayed) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(R.id.price_text_view, isDisplayed);
	}

	@And("^Validate that airline name field is displayed: (true|false)$")
	public void checkVisibilityOfAirlineName(boolean isDisplayed) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(R.id.airline_text_view, isDisplayed);
	}

	@And("^Validate that flight duration field is displayed: (true|false)$")
	public void checkVisibilityOfFlightDuration(boolean isDisplayed) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(R.id.flight_duration_text_view, isDisplayed);
	}

	@And("^Validate that round trip header is displayed: (true|false)$")
	public void checkVisibilityOfRoundTripHeader(boolean isDisplayed) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(R.id.trip_type_text_view, isDisplayed);
	}

	private void validateFlightSRPListViewCellItemVisibility(int resId, boolean isDisplayed) {
		onView(withId(R.id.list_view)).check(RecyclerViewAssertions.assertionOnItemAtPosition(2, hasDescendant(
			allOf(withId(resId), (isDisplayed ? isDisplayed() : not(isDisplayed()))))));
	}

	private void checkString(int resID, String text) {
		onView(withId(R.id.list_view)).check(RecyclerViewAssertions.assertionOnItemAtPosition(2, hasDescendant(
			allOf(withId(resID), withText(containsString(text))))));
	}

	@And("^Name of airline is \"(.*?)\"$")
	public void checkAirlineName(String airline) throws Throwable {
		checkString(R.id.airline_text_view, airline);
	}

	@And("^Price of the flight is (\\d+)$")
	public void checkPriceOfFlight(int price) throws Throwable {
		checkString(R.id.price_text_view, String.valueOf(price));
	}

	@And("^Duration of the flight is \"(.*?)\"$")
	public void checkDuraionOfFlight(String duration) throws Throwable {
		checkString(R.id.flight_duration_text_view, duration);
	}

	@And("^Timing of the flight is \"(.*?)\"$")
	public void checkTimingOfTheFlight(String timing) throws Throwable {
		checkString(R.id.flight_time_detail_text_view, timing);
	}

	@And("^Number of stops are (\\d+)$")
	public void numberOfStops(int stops) throws Throwable {
		if (stops > 0) {
			checkString(R.id.flight_duration_text_view, (String.valueOf(stops) + " Stop"));
		}
		else {
			checkString(R.id.flight_duration_text_view, "Nonstop");
		}
	}

	@And("^the currency symbol on FSR is \"(.*?)\"$")
	public void checkCurrencyOnFSR(String currencySymbol) throws Throwable {
		checkString(R.id.price_text_view, currencySymbol);
	}

}
