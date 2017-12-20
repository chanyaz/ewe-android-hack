package com.expedia.bookings.test.stepdefs.phone.flights;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.Matchers;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen;
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen;
import com.expedia.bookings.test.stepdefs.phone.TestUtil;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.Common.delay;
import static com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable;
import static com.expedia.bookings.test.espresso.CustomMatchers.withIndex;
import static com.expedia.bookings.test.espresso.ViewActions.getString;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyDetailsAmenitiesDialog;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyDetailsBundleTotalPrice;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyDetailsWidgetAirlinesLabel;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyDetailsWidgetDoneBtn;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyDetailsWidgetFarelist;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyDetailsWidgetLocationLabel;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyDetailsWidgetTitle;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyTravellerNumber;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyWidget;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyWidgetDeltaPrice;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyWidgetFromLabel;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyWidgetSubtitle;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyWidgetTitle;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.fareFamilyWidgetIcon;
import static com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen.flightOverviewBundleTotalPrice;
import static com.expedia.bookings.test.stepdefs.phone.TestUtil.getViewText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

public class FlightsOverviewScreenSteps {
	@And("^I click on checkout button$")
	public void clickOnCheckoutButton() throws Throwable {
		FlightsOverviewScreen.clickOnCheckoutButton();
	}

	@And("^Close price change Alert dialog$")
	public void closeAlertDialog() throws Throwable {
		SearchScreen.searchAlertDialogDone().perform(click());
	}

	@And("^Validate that alert Dialog Box with title \"(.*?)\" is visible$")
	public void validatePriceChangeDialogHeading(String heading) throws Throwable {
		delay(1);
		onView(withText(heading)).check(matches(isDisplayed()));
	}

	@And("^Validate Price Change to \"(.*?)\" from \"(.*?)\"$")
	public void validatePriceChangeDialogString(String newPrice, String oldPrice) throws Throwable {
		onView(allOf(withId(android.R.id.message), withText("The price of your trip has changed from " + oldPrice + " to "
			+ newPrice + ". Rates can change frequently. Book now to lock in this price.")))
			.check(matches(isDisplayed()));
	}

	@And("^Check if Trip total is \"(.*?)\" on Price Change$")
	public void validateTripTotalOnPriceChange(String finalPrice) throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.total_price_widget)), withId(R.id.bundle_total_price), withText(finalPrice))).check(matches(isDisplayed()));
	}

	@And("^Check if Cost Summary Dialog Box has \"(.*?)\" as Final Price$")
	public void validateCostSummaryPriceChange(String finalPrice) throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.total_price_widget)), withId(R.id.bundle_total_text))).perform(click());
		onView(Matchers.allOf(withId(R.id.price_type_text_view), withText("Total Due Today"), hasSibling(withText(finalPrice)))).check(matches(isDisplayed()));
		closeAlertDialog();
	}

	@And("^Click on \"(.*?)\" button$")
	public void clickErrorButton(String errorButtonText) {
		onView(allOf(withId(R.id.error_action_button), withText(errorButtonText))).perform(click());
	}

	@Then("^toggle the outbound widget$")
	public void toggleOutboundWidget() throws Throwable {
		onView(allOf(withParent(withParent(withParent((withId(R.id.package_bundle_outbound_flight_widget))))),
				withId(R.id.package_flight_details_icon))).perform(click());
	}
	@Then("^toggle the inbound widget$")
	public void toggleInboundWidget() throws Throwable {
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
	@Then("^validate free cancellation message \"(.*?)\" is displayed$")
	public void validateFreeCancellation(String message) throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.free_cancellation_layout)), withText(message)))
				.check(matches(isDisplayed()));
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
		onView(allOf(isDescendantOfA(withId(R.id.total_price_widget)), withId(R.id.bundle_total_price))).check(matches(withText(price)));
	}
	@Then("^I click on trip total link$")
	public void clickTripTotal() throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.total_price_widget)), withId(R.id.bundle_total_text))).perform(click());
	}
	@Then("^validate following detailed information is present on cost summary screen$")
	public void validateCostSummaryPopup(Map<String, String> params) throws Throwable {
		validateCostSummaryPriceDetails("Adult 1 details", params.get("Adult 1 details"));
		validateCostSummaryPriceDetails("Flight", params.get("Flight"));
		validateCostSummaryPriceDetails("Taxes & Fees", params.get("Taxes & Fees"));
		validateCostSummaryPriceDetails(BuildConfig.brand + " Booking Fee", params.get("Booking Fee"));
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
	@And("^validate price for \"([^\"]*)\" is displayed$")
	public void validatePriceTotalDueToday(String price) throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.breakdown_container)), withId(R.id.price_type_text_view),
			withText(price), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).perform(scrollTo())
			.check(matches(allOf(hasSibling(withId(R.id.price_text_view)), isDisplayed())));
	}

	@And("^validate Booking Fee text is displayed$")
	public void validateBookingFeeText() throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.breakdown_container)), withId(R.id.price_type_text_view),
			withText(BuildConfig.brand + " Booking Fee"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).perform(scrollTo())
			.check(matches(allOf(hasSibling(withId(R.id.price_text_view)), isDisplayed())));
	}

	@Then("^Validate if error-toolbar has text \"(.*?)\"$")
	public void validateErrorToolbarText(String errorToolbarText) {
		onView(allOf(withId(R.id.error_toolbar), hasDescendant(withText(errorToolbarText)))).check(matches(isDisplayed()));
	}

	@Then("^Validate if error image is of \"(.*?)\"$")
	public void validateErrorImage(String imageDesc) {
		if (imageDesc.equals("Expedia")) {
			onView(withId(R.id.error_image)).check(matches(withImageDrawable(R.drawable.error_default)));
		}
		else if (imageDesc.equals("Watch")) {
			onView(withId(R.id.error_image)).check(matches(withImageDrawable(R.drawable.error_timeout)));
		}
	}

	@Then("^Validate that error-action-button is present and have text \"(.*?)\"$")
	public void validateErrorButtonText(String errorButtonText) {
		onView(allOf(withId(R.id.error_action_button), withText(errorButtonText))).check(matches(isDisplayed()));
	}

	@Then("^Validate that error text is \"(.*?)\"$")
	public void validateErrorText(String errorText) {
		onView(allOf(withId(R.id.error_text), withText(errorText))).check(matches(isDisplayed()));
	}

	@Then("^Validate \"(.*?)\" is present on the overview screen for isOutbound : (true|false)$")
	public void validatePreferredClassOnOverview(String preferredClass, boolean outBound) throws Throwable {
		validateFlightInfoWithMoreInfo(R.id.flight_class_text_view, preferredClass, outBound);
	}

	private void validateFlightInfoWithMoreInfo(int resId, String parameter, boolean outBound) throws Throwable {
		onView(Matchers.allOf(outBound ? isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))
				: isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)),
			withId(R.id.card_view)))
			.check(matches(hasDescendant(allOf(withId(resId), withText(containsString(parameter))))));
	}
	@Then("^Validate that fare family widget card is displayed$")
	public void validateFareFamilyCard() {
		fareFamilyWidget().check(matches(isDisplayed()));
	}

	@Then("^I click on fare family widget card$")
	public void clickFareFamilyCard() {
		fareFamilyWidget().perform(click());
	}

	@Then("^Validate fare family widget card info$")
	public void validateFareFamilyCardInfo(Map<String, String> params) throws Throwable {
		final AtomicReference<String> subtitle = new AtomicReference<String>();
		validateFareFamilyIcon();
		fareFamilyWidgetTitle().check(matches(isDisplayed())).check(matches(withText(params.get("title"))));
		fareFamilyWidgetSubtitle().check(matches(isDisplayed()));
		fareFamilyWidgetSubtitle().perform(getString(subtitle));
		assert (subtitle.toString().contains(params.get("subtitle")));

		if (params.get("from_label") != null) {
			fareFamilyWidgetFromLabel().check(matches(isDisplayed())).check(matches(withText(params.get("from_label"))));
		}
		if (params.get("delta_price") != null) {
			getViewText(fareFamilyWidgetDeltaPrice()).contains(params.get("delta_price"));
		}
	}

	@Then("^Validate that traveller number is visible on fare family card : (true|false)$")
	public void validateFareFamilyTravellerNumberVisibility(boolean isTravellerNumberVisible) {
		fareFamilyTravellerNumber().check(matches( isTravellerNumberVisible ? isDisplayed() : not(isDisplayed())));
	}

	@Then("^Validate that \"(.*?)\" are displayed on fare family card")
	public void validateFareFamilyTravellerNumberString(String travellerNumberString) {
		fareFamilyTravellerNumber().check(matches(withText(travellerNumberString)));
	}

	@Then("^Validate fare family details header info$")
	public void validateFareFamilyDetailsHeaderInfo(Map<String, String> params) throws Throwable {
		fareFamilyDetailsWidgetTitle(params.get("title")).check(matches(isDisplayed()));
		fareFamilyDetailsWidgetLocationLabel().check(matches(withText(params.get("location")))).check(matches(isDisplayed()));
		fareFamilyDetailsWidgetAirlinesLabel().check(matches(withText(params.get("airline")))).check(matches(isDisplayed()));
	}

	@Then("^I select flight upgrade at position (\\d+)$")
	public void selectFlightUpgrade(int position) throws Throwable {
		int upsellCount = EspressoUtils.getListChildCount(fareFamilyDetailsWidgetFarelist());
		assert (upsellCount > 1);
		onView(withIndex(withId(R.id.fare_family_class_header), position - 1)).perform(click());
	}
	@Then("^I click on done button for upsell$")
	public void clickDoneButton() throws Throwable {
		fareFamilyDetailsWidgetDoneBtn().perform(click());
	}

	@Then("^I click on show more amenities at position (\\d+)$")
	public void showMoreAmenitiesAtPosition(int position) throws Throwable {
		onView(withIndex(withId(R.id.fare_family_show_more_container), position - 1)).perform(click());
	}

	@Then("^Validate amenities dialog is visible$")
	public void assertAmenitiesDialogVisibility() throws Throwable {
		fareFamilyDetailsAmenitiesDialog().perform(waitForViewToDisplay()).check(matches(isDisplayed()));
	}

	@Then("^I store the data in \"(.*?)\"$")
	public void storeVariable(String key) throws Throwable {
		TestUtil.storeDataAtRuntime.put(key, getViewText(fareFamilyDetailsBundleTotalPrice()));
	}

	@Then("^Validate fare family card title is \"(.*?)\"$")
	public void validateupsellCardTitle(String title) {
		fareFamilyWidgetTitle().check(matches(isDisplayed()));
		getViewText(fareFamilyWidgetTitle()).contains(title);
	}

	@Then("^Validate fare family card icon is displayed$")
	public void validateFareFamilyIcon() {
		fareFamilyWidgetIcon().check(matches(isDisplayed()));
		fareFamilyWidgetIcon().check(matches(withImageDrawable(R.drawable.flight_upsell_seat_icon)));
	}

	@Then("^Validate fare family card subtitle is \"(.*?)\"$")
	public void validateupsellCardSubtitle(String subtitle) {
		fareFamilyWidgetSubtitle().check(matches(isDisplayed()));
		getViewText(fareFamilyWidgetSubtitle()).contains(subtitle);
	}

	@Then("^Validate delta price is displayed : (true|false)$")
	public void validateDeltaPriceIsDisplayed(boolean check) {
		fareFamilyWidgetDeltaPrice().check(matches( check ? isDisplayed() : not(isDisplayed())));
	}

	@Then("^Validate from label is displayed : (true|false)$")
	public void validateroundTripLabelIsDisplayed(boolean check) {
		fareFamilyWidgetFromLabel().check(matches( check ? isDisplayed() : not(isDisplayed())));
	}

	@Then("^Validate bundle total amount is \"(.*?)\"$")
	public void validateBundleTotalAmount(String variable) {
		flightOverviewBundleTotalPrice().check(matches(isDisplayed())).check(matches(withText(TestUtil.storeDataAtRuntime.get(variable))));
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
