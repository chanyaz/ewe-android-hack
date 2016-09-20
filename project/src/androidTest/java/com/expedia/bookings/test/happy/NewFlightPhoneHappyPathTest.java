package com.expedia.bookings.test.happy;

import java.lang.reflect.Method;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.NewFlightTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.newflights.FlightTestHelpers;
import com.expedia.bookings.test.phone.newflights.FlightsResultsScreen;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class NewFlightPhoneHappyPathTest extends NewFlightTestCase {

	@Override
	public void runTest() throws Throwable {
		Method method = getClass().getMethod(getName(), (Class[]) null);
		if (method.getName().equals("testNewFlightHappyPath")) {
			Intents.init();
			intending(hasComponent(WebViewActivity.class.getName()))
				.respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
			bucketInsuranceTest(true);
		}
		else {
			bucketInsuranceTest(false);
		}
		super.runTest();
	}

	@Override
	protected void tearDown() throws Exception {
		Method method = getClass().getMethod(getName(), (Class[]) null);

		if (method.getName().equals("testNewFlightHappyPath")) {
			Intents.release();
		}
		super.tearDown();
	}

	public void testNewFlightHappyPath() throws Throwable {
		SearchScreen.origin().perform(click());
		SearchScreen.selectFlightOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);

		SearchScreen.searchButton().perform(click());

		FlightTestHelpers.assertFlightOutbound();
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0);
		FlightsScreen.selectOutboundFlight().perform(click());

		FlightTestHelpers.assertFlightInbound();
		FlightTestHelpers.assertDockedOutboundFlightSelectionWidget();
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("Outbound");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("Delta");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("9:00 pm - 11:00 pm (2h 0m)");
		FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0);
		FlightsScreen.selectInboundFlight().perform(click());

		assertCheckoutOverview();
		assertCostSummaryView();

		// move to Flight/common screen
		PackageScreen.checkout().perform(click());

		assertInsuranceIsVisible();
		PackageScreen.showInsuranceBenefits();
		assertInsuranceBenefits();
		PackageScreen.showInsuranceTerms();
		assertInsuranceTerms();
		PackageScreen.toggleInsurance();
		assertInsuranceIsAdded();
		PackageScreen.toggleInsurance();
		assertInsuranceIsRemoved();

		PackageScreen.travelerInfo().perform(scrollTo(), click());
		PackageScreen.enterFirstName("Eidur");
		PackageScreen.enterLastName("Gudjohnsen");
		PackageScreen.enterEmail("test@gmail.com");
		Espresso.closeSoftKeyboard();
		PackageScreen.enterPhoneNumber("4155554321");
		Espresso.closeSoftKeyboard();
		PackageScreen.selectBirthDate(1989, 6, 9);
		PackageScreen.selectGender("Male");
		PackageScreen.clickTravelerAdvanced();
		PackageScreen.enterRedressNumber("1234567");
		PackageScreen.clickTravelerDone();

		PackageScreen.clickPaymentInfo();
		assertInsuranceIsNotVisible();
		PackageScreen.enterCreditCard();
		PackageScreen.completePaymentForm();
		PackageScreen.clickPaymentDone();
		PackageScreen.clickLegalInformation();

		assertLegalInformation();
		Common.pressBack();

		// TODO - assert checkout overview information
		CheckoutViewModel.performSlideToPurchase();

		assertConfirmationView();
	}

	public void testNewFlightHappyPathSignedIn() throws Throwable {
		selectOriginDestinationAndDates();

		SearchScreen.searchButton().perform(click());
		selectFirstOutboundFlight();
		selectFirstInboundFlight();

		assertCheckoutOverview();
		assertCostSummaryView();

		PackageScreen.checkout().perform(click());

		CheckoutViewModel.signInOnCheckout();
		Common.delay(1);

		CheckoutViewModel.clickPaymentInfo();
		CheckoutViewModel.selectStoredCard("Saved AmexTesting");
		Common.pressBack();
		CheckoutViewModel.performSlideToPurchase(true);
		assertSignedInConfirmationView();
	}

	private void assertDockedOutboundWidgetShown() {
		FlightTestHelpers.assertDockedOutboundFlightSelectionWidget();
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("Outbound");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("Delta");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("9:00 pm - 11:00 pm (2h 0m)");
	}

	private void enterGuestTravelerDetails() {
		PackageScreen.travelerInfo().perform(scrollTo(), click());
		PackageScreen.enterFirstName("Eidur");
		PackageScreen.enterLastName("Gudjohnsen");
		PackageScreen.enterEmail("test@gmail.com");
		PackageScreen.enterPhoneNumber("4155554321");
		PackageScreen.selectBirthDate(1989, 6, 9);
		PackageScreen.selectGender("Male");
		PackageScreen.clickTravelerAdvanced();
		PackageScreen.enterRedressNumber("1234567");
	}

	private void selectFirstInboundFlight() {
		FlightTestHelpers.assertFlightInbound();
		FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0);
		FlightsScreen.selectInboundFlight().perform(click());
	}

	private void selectFirstOutboundFlight() {
		FlightTestHelpers.assertFlightOutbound();
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0);
		FlightsScreen.selectOutboundFlight().perform(click());
	}

	private void selectOriginDestinationAndDates() throws Throwable {
		SearchScreen.origin().perform(click());
		SearchScreen.selectFlightOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
	}

	private void assertCostSummaryView() {
		String totalDueToday = "$696.00";
		String adultOneTotal = "$689.00";
		String taxesAndFees = "$68.54";
		onView(withId(R.id.bundle_total_text)).perform(click());
		onView(withText("Adult 1 details")).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));
		onView(withText(adultOneTotal)).check(matches(isDisplayed()));
		onView(withText("Flight")).check(matches(isDisplayed()));
		onView(withText("Taxes & Fees")).check(matches(isDisplayed()));
		onView(withText(taxesAndFees)).check(matches(isDisplayed()));
		onView(withText("Total Due Today")).check(matches(isDisplayed()));
		onView(withText(totalDueToday)).check(matches(isDisplayed()));
		onView(withId(android.R.id.button1)).perform(click());
	}

	private void assertLegalInformation() {
		onView(withId(R.id.rules_and_restrictions)).perform(ViewActions.waitForViewToDisplay())
			.check(matches(isDisplayed()));
		onView(withId(R.id.terms_and_conditions)).check(matches(isDisplayed()));
		onView(withId(R.id.privacy_policy)).check(matches(isDisplayed()));
		onView(withId(R.id.liabilities_link_text_view)).check(matches(isDisplayed()));
	}

	private void assertConfirmationView() {

		onView(withId(R.id.confirmation_container)).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.destination),
			isDescendantOfA(withId(R.id.confirmation_container)))).check(
			matches(withText("Detroit")));

		onView(allOf(withId(R.id.expedia_points),
			isDescendantOfA(withId(R.id.confirmation_container)))).check(
			matches(not(isDisplayed())));

		onView(allOf(withId(R.id.first_row),
			isDescendantOfA(withId(R.id.outbound_flight_card)))).check(
			matches(withText("Flight to (DTW) Detroit")));

		onView(allOf(withId(R.id.first_row),
			isDescendantOfA(withId(R.id.inbound_flight_card)))).check(
			matches(withText("Flight to (SFO) San Francisco")));

		onView(allOf(withId(R.id.hotel_cross_sell_widget),
			isDescendantOfA(withId(R.id.confirmation_container)))).check(
			matches(isDisplayed()));

		onView(allOf(withId(R.id.air_attach_countdown_view),
			isDescendantOfA(withId(R.id.hotel_cross_sell_widget)))).check(
			matches(isDisplayed()));

		onView(allOf(withId(R.id.air_attach_expires_today_text_view),
			isDescendantOfA(withId(R.id.hotel_cross_sell_widget)))).check(
			matches(not(isDisplayed())));
	}

	private void assertSignedInConfirmationView() {

		onView(withId(R.id.confirmation_container)).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.destination),
			isDescendantOfA(withId(R.id.confirmation_container)))).check(
			matches(withText("Detroit")));

		onView(allOf(withId(R.id.expedia_points),
			isDescendantOfA(withId(R.id.confirmation_container)))).check(
			matches(isDisplayed()));

		onView(allOf(withId(R.id.first_row),
			isDescendantOfA(withId(R.id.outbound_flight_card)))).check(
			matches(withText("Flight to (DTW) Detroit")));

		onView(allOf(withId(R.id.first_row),
			isDescendantOfA(withId(R.id.inbound_flight_card)))).check(
			matches(withText("Flight to (SFO) San Francisco")));

		onView(allOf(withId(R.id.hotel_cross_sell_widget),
			isDescendantOfA(withId(R.id.confirmation_container)))).check(
			matches(isDisplayed()));

		onView(allOf(withId(R.id.air_attach_countdown_view),
			isDescendantOfA(withId(R.id.hotel_cross_sell_widget)))).check(
			matches(isDisplayed()));

		onView(allOf(withId(R.id.air_attach_expires_today_text_view),
			isDescendantOfA(withId(R.id.hotel_cross_sell_widget)))).check(
			matches(not(isDisplayed())));
	}

	private void assertConfirmationViewForOneWay() {
		onView(withId(R.id.confirmation_container)).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.destination),
			isDescendantOfA(withId(R.id.confirmation_container)))).check(
			matches(withText("Detroit")));

		onView(allOf(withId(R.id.expedia_points),
			isDescendantOfA(withId(R.id.confirmation_container)))).check(
			matches(isDisplayed()));

		onView(allOf(withId(R.id.first_row),
			isDescendantOfA(withId(R.id.outbound_flight_card)))).check(
			matches(withText("Flight to (DTW) Detroit")));

		onView(allOf(withId(R.id.first_row),
			isDescendantOfA(withId(R.id.inbound_flight_card)))).check(
			matches(not(isDisplayed())));

		onView(allOf(withId(R.id.hotel_cross_sell_widget),
			isDescendantOfA(withId(R.id.confirmation_container)))).check(
			matches(isDisplayed()));

		onView(allOf(withId(R.id.air_attach_countdown_view),
			isDescendantOfA(withId(R.id.hotel_cross_sell_widget)))).check(
			matches(isDisplayed()));

		onView(allOf(withId(R.id.air_attach_expires_today_text_view),
			isDescendantOfA(withId(R.id.hotel_cross_sell_widget)))).check(
			matches(not(isDisplayed())));
	}

	private void assertCheckoutOverview() {
		onView(allOf(withId(R.id.destination), withParent(withId(R.id.checkout_overview_floating_toolbar)),
			withText("Detroit, MI"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.travelers), withParent(withId(R.id.checkout_overview_floating_toolbar)),
			withText("1 Traveler"))).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.flight_card_view_text),
			isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)))).check(
			matches(withText("Flight to (DTW) Detroit")));

		onView(allOf(withId(R.id.flight_card_view_text),
			isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)))).check(
			matches(withText("Flight to (SFO) San Francisco")));

		onView(allOf(withId(R.id.bundle_total_includes_text), isDescendantOfA(withId(R.id.total_price_widget)),
			withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
			withText(
				"Includes taxes and fees"))).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.bundle_total_text), isDescendantOfA(withId(R.id.total_price_widget)),
			withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
			withText(
				"Trip Total"))).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.bundle_total_savings), isDescendantOfA(withId(R.id.total_price_widget)),
			withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

	}

	private void assertInsuranceBenefits() {
		onView(withId(R.id.insurance_benefits_dialog_body)).check(matches(isDisplayed()))
			.perform(pressBack()).check(doesNotExist());
	}

	private void assertInsuranceIsAdded() {
		PackageScreen.showPriceBreakdown();
		onView(withText(R.string.cost_summary_breakdown_flight_insurance)).check(matches(isDisplayed()));
		Espresso.pressBack();
		onView(withId(R.id.insurance_title)).check(matches(withText("Your trip is protected for $19/person")));
	}

	private void assertInsuranceIsNotVisible() {
		onView(withId(R.id.insurance_widget)).check(matches(not(isDisplayed())));
	}

	private void assertInsuranceIsVisible() {
		onView(withId(R.id.insurance_widget)).check(matches(isDisplayed()));
	}

	private void assertInsuranceIsRemoved() {
		PackageScreen.showPriceBreakdown();
		onView(withText(R.string.cost_summary_breakdown_flight_insurance)).check(doesNotExist());
		Espresso.pressBack();
		onView(withId(R.id.insurance_title)).check(matches(withText("Add protection for $19/person")));
	}

	private void assertInsuranceTerms() {
		intended(hasComponent(WebViewActivity.class.getName()));
	}

	private void bucketInsuranceTest(boolean bucket) {
		if (bucket) {
			AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppFlightInsurance,
				AbacusUtils.DefaultVariate.BUCKETED.ordinal());
		}
		else {
			AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppFlightInsurance,
				AbacusUtils.DefaultVariate.CONTROL.ordinal());
		}
	}

	private void assertCheckoutOverviewForOneway() {
		onView(allOf(withId(R.id.destination), withParent(withId(R.id.checkout_overview_floating_toolbar)),
			withText("Detroit, MI (DTW-Detroit Metropolitan Wayne County)"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.travelers), withParent(withId(R.id.checkout_overview_floating_toolbar)),
			withText("1 Traveler"))).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.flight_card_view_text),
			isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)))).check(
			matches(withText("Flight to (DTW) Detroit")));
	}
}
