package com.expedia.bookings.test.happy;

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

public class NewFlightPhoneHappyPathTest extends NewFlightTestCase {

	@Override
	public void runTest() throws Throwable {
		Intents.init();
		intending(hasComponent(WebViewActivity.class.getName()))
			.respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
		super.runTest();
	}

	public void testNewFlightHappyPath() throws Throwable {
		bucketInsuranceTest();

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

		assertInsuranceIsAvailable();
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
		PackageScreen.enterPhoneNumber("4155554321");
		PackageScreen.selectBirthDate(1989, 6, 9);

		PackageScreen.selectGender("Male");

		PackageScreen.clickTravelerAdvanced();
		PackageScreen.enterRedressNumber("1234567");

		PackageScreen.clickTravelerDone();
		PackageScreen.enterPaymentInfo();

		PackageScreen.clickLegalInformation();
		assertLegalInformation();
		Common.pressBack();

		// TODO - assert checkout overview information

		CheckoutViewModel.performSlideToPurchase();

		assertConfirmationView();
	}

	private void assertCostSummaryView() {
		onView(withId(R.id.bundle_total_text)).perform(click());
		onView(withText("Adult 1 details")).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));
		onView(withText("$689.00")).check(matches(isDisplayed()));
		onView(withText("Total Due Today")).check(matches(isDisplayed()));
		onView(withText("$696.00")).check(matches(isDisplayed()));
		onView(withId(android.R.id.button1)).perform(click());
	}

	private void assertLegalInformation() {
		onView(withId(R.id.rules_and_restrictions)).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));
		onView(withId(R.id.terms_and_conditions)).check(matches(isDisplayed()));
		onView(withId(R.id.privacy_policy)).check(matches(isDisplayed()));
		onView(withId(R.id.liabilities_link_text_view)).check(matches(isDisplayed()));
	}

	private void assertConfirmationView() {
		onView(withId(R.id.confirmation_container)).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));
	}

	private void assertCheckoutOverview() {
		onView(allOf(withId(R.id.destination), withParent(withId(R.id.checkout_overview_floating_toolbar)),
			withText("Detroit, MI (DTW-Detroit Metropolitan Wayne County)"))).check(matches(isDisplayed()));
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

	}

	private void assertInsuranceBenefits() {
		onView(withId(R.id.insurance_benefits_dialog_body)).check(matches(isDisplayed()))
			.perform(pressBack()).check(doesNotExist());
	}

	private void assertInsuranceIsAdded() {
		onView(withId(R.id.insurance_title)).check(matches(withText("Your trip is protected for $19/person")));
		PackageScreen.showPriceBreakdown();
		onView(withText(R.string.cost_summary_breakdown_flight_insurance)).check(matches(isDisplayed()));
		Espresso.pressBack();
	}

	private void assertInsuranceIsAvailable() {
		onView(withId(R.id.insurance_widget)).check(matches(isDisplayed()));
	}

	private void assertInsuranceIsRemoved() {
		onView(withId(R.id.insurance_title)).check(matches(withText("Add protection for $19/person")));
		PackageScreen.showPriceBreakdown();
		onView(withText(R.string.cost_summary_breakdown_flight_insurance)).check(doesNotExist());
		Espresso.pressBack();
	}

	private void assertInsuranceTerms() {
		intended(hasComponent(WebViewActivity.class.getName()));
	}

	private void bucketInsuranceTest() {
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightInsurance);
	}
}
