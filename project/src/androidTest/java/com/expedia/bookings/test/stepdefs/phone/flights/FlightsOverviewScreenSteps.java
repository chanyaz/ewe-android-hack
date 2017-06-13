package com.expedia.bookings.test.stepdefs.phone.flights;

import com.expedia.bookings.R;


import com.expedia.bookings.test.phone.newflights.FlightsOverviewScreen;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;
import com.expedia.bookings.test.stepdefs.phone.TestUtil;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

import android.support.test.espresso.matcher.ViewMatchers;

import static org.hamcrest.Matchers.not;
import org.hamcrest.Matchers;

import java.util.Map;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;

public class FlightsOverviewScreenSteps {
	@And("^I click on checkout button$")
	public void clickOnCheckoutButton() throws Throwable {
		FlightsOverviewScreen.clickOnCheckoutButton();
	}
	@Then("^collapse the outbound widget$")
	public void collapseOutboundWidget() throws Throwable {
		onView(allOf(withParent(withParent(withParent((withId(R.id.package_bundle_outbound_flight_widget))))),
				withId(R.id.package_flight_details_icon))).perform(click());
	}
	@Then("^collapse the inbound widget$")
	public void collapseInboundWidget() throws Throwable {
		onView(allOf(withParent(withParent(withParent((withId(R.id.package_bundle_inbound_flight_widget))))),
				withId(R.id.package_flight_details_icon))).perform(click());
	}
	@Then("^validate following information is present on the overview screen for isOutbound : (true|false)$")
	public void validateOverviewInfo(boolean outBound, Map<String, String> parameters) throws Throwable {
		validateFlightOverviewWidget(R.id.flight_card_view_text, parameters.get("destination"), outBound);
		validateFlightOverviewWidget(R.id.travel_info_view_text, parameters.get("travel date and traveller"), outBound);
		validateFlightInfo(R.id.departure_arrival_time, parameters.get("Flight time"), outBound);
		validateFlightInfo(R.id.departure_arrival_airport, parameters.get("airport names"), outBound);
		validateFlightInfo(R.id.airline_airplane_type, parameters.get("airline name"), outBound);
		validateFlightInfo(R.id.flight_duration, parameters.get("flight duration"), outBound);
	}
	@Then("^validate total duration on flight Overview is \"([^\"]*)\" for isOutbound : (true|false)$")
	public void validateOutboundFlightTotalDuration(String totalDuration, boolean outBound) throws Throwable {
		onView(Matchers.allOf(outBound ? isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))
						: isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)),
				withId(R.id.segment_breakdown)))
				.check(matches(hasSibling(allOf(withId(R.id.flight_total_duration), withText(containsString(totalDuration))))));
	}
	@Then("^select outbound flight from SRP$")
	public void selectOutboundFlight() throws Throwable {
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 1);
		FlightsScreen.selectOutboundFlight().perform(click());
	}

	@Then("^validate following flight details for multi-leg flights$")
	public void validateSegmentFlight(Map<String, String> parameters) throws Throwable {
		validateSegmentFlight(R.id.departure_arrival_time, parameters.get("first-segment-flight time"));
		validateSegmentFlight(R.id.departure_arrival_airport, parameters.get("first-segment-airport name"));
		validateSegmentFlight(R.id.airline_airplane_type, parameters.get("first-segment-airline name"));
		validateSegmentFlight(R.id.flight_duration, parameters.get("first-segment-flight duration"));
		validateSegmentFlight(R.id.departure_arrival_time, parameters.get("second-segment-flight time"));
		validateSegmentFlight(R.id.departure_arrival_airport, parameters.get("second-segment-airport name"));
		validateSegmentFlight(R.id.airline_airplane_type, parameters.get("second-segment-airline name"));
		validateSegmentFlight(R.id.flight_duration, parameters.get("second-segment-flight duration"));
	}
	@Then("^validate layover of outbound flight is on \"([^\"]*)\" for \"([^\"]*)\"$")
	public void validateLayoverFlight(String layoverPlace, String layoverTime) throws Throwable {
		onView(Matchers.allOf(withParent(withParent(withParent(withParent(withId(R.id.package_bundle_outbound_flight_widget))))),
				withId(R.id.breakdown_container)))
				.check(matches(hasDescendant(allOf(withId(R.id.flight_segment_layover_in), withText(containsString(layoverPlace))))));
		onView(Matchers.allOf(withParent(withParent(withParent(withParent(withId(R.id.package_bundle_outbound_flight_widget))))),
				withId(R.id.breakdown_container)))
				.check(matches(hasDescendant(allOf(withId(R.id.flight_segment_layover_duration), withText(layoverTime)))));
	}
	@Then("^validate free cancellation message is displayed$")
	public void validateFreeCancellation() throws Throwable {
		onView(withId(R.id.free_cancellation_text))
				.check(matches(allOf(withText(R.string.flights_free_cancellation), isDisplayed())));
	}
	@Then("^validate split ticket messaging is displayed$")
	public void validateSplitTicketMessage() throws Throwable {
		onView(withId(R.id.split_ticket_info_container))
				.check(matches(hasDescendant(allOf(withId(R.id.split_ticket_rules_and_restrictions), withText
						(R.string.split_ticket_rules_with_link_TEMPLATE),
						isDisplayed()))));
		onView(withId(R.id.split_ticket_info_container))
				.check(matches(hasDescendant(allOf(withId(R.id.split_ticket_cancellation_policy), withText
								(R.string.split_ticket_rules_cancellation_policy),
						isDisplayed()))));
		onView(withId(R.id.split_ticket_info_container))
				.check(matches(hasDescendant(allOf(withId(R.id.split_ticket_baggage_fee_links), withText
								("Departure and Return flights have their own baggage fees"),
						isDisplayed()))));
	}
	@Then("^validate total price of the trip is \"([^\"]*)\"$")
	public void validateTotalPrice(String price) throws Throwable {
		onView(withId(R.id.bundle_total_price)).check(matches(withText(price)));
	}
	@Then("^I click on trip total link$")
	public void clickTripTotal() throws Throwable {
		onView(withId(R.id.bundle_total_text)).perform(click());
	}
	@Then("^validate following detailed information is present on cost summary screen$")
	public void validateCostSummaryPopup(Map<String, String> params) throws Throwable {
		validateCostSummaryPriceDetails("Adult 1 details", params.get("Adult 1 details"));
		validateCostSummaryPriceDetails("Flight", params.get("Flight"));
		validateCostSummaryPriceDetails("Taxes & Fees", params.get("Taxes & Fees"));
		validateCostSummaryPriceDetails("Expedia Booking Fee", params.get("Expedia Booking Fee"));
		validateCostSummaryPriceDetails("Total Due Today", params.get("Total Due Today"));
	}
	@Then("^basic economy link with text \"([^\"]*)\" isDisplayed : (true|false)$")
	public void verifyBasicEconomy(String linkText,boolean isDisplayed) throws Throwable {
		onView(allOf(withId(R.id.basic_economy_info), withText(linkText)))
			.check(matches(allOf(withText(linkText),isDisplayed ? isDisplayed() : not(isDisplayed()))));
	}

	@Then("^validate price info for multi travellers$")
	public void validatePriceOfMultiTravellers() throws Throwable {
		Map<String, String> travellers = TestUtil.dataSet;
		int adult = Integer.parseInt(travellers.get("adults"));
		int child = Integer.parseInt(travellers.get("child"));

		for (int i = 1; i <= adult; i++) {
			onView(allOf(isDescendantOfA(withId(R.id.breakdown_container)), withId(R.id.price_type_text_view),
				withText("Adult " + i + " details"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
				.check(matches(allOf(hasSibling(withId(R.id.price_text_view)), isDisplayed())));
			}
		for (int i = 1; i <= child; i++) {
			onView(allOf(isDescendantOfA(withId(R.id.breakdown_container)), withId(R.id.price_type_text_view),
				withText("Child " + i + " details"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
				.check(matches(allOf(hasSibling(withId(R.id.price_text_view)), isDisplayed())));
			}
	}
	@Then("^validate price for \"([^\"]*)\" is displayed$")
	public void validatePriceTotalDueToday(String price) throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.breakdown_container)), withId(R.id.price_type_text_view),
			withText(price), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).perform(scrollTo())
			.check(matches(allOf(hasSibling(withId(R.id.price_text_view)), isDisplayed())));
	}
	private void validateFlightInfo(int resId, String parameter, boolean outBound) throws Throwable {
		onView(Matchers.allOf(outBound ? isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))
						: isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)),
				withId(R.id.breakdown_container)))
				.check(matches(hasDescendant(allOf(withId(resId), withText(containsString(parameter))))));
	}
	private void validateFlightOverviewWidget(int resId, String value, boolean outBound) throws Throwable {
		onView(allOf(outBound ? isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))
						: isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
				withId(resId)))
				.check(matches(withText(containsString(value))));
	}
	private void validateSegmentFlight(int resId, String value) throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)),
			withId(resId), withText(containsString(value)), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
				.check(matches((withText(containsString(value)))));
	}
	private void validateCostSummaryPriceDetails(String priceType, String priceAmt)throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.breakdown_container)), withId(R.id.price_type_text_view),
				withText(priceType), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
				.check(matches(allOf(withId(R.id.price_type_text_view),
						withText(priceType), hasSibling(withText(priceAmt)), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
				)));
	}
}
