package com.expedia.bookings.test.happy;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Test;

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
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.NewFlightTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.TravelerModel.SaveTravelerPrompt;
import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails;
import com.expedia.bookings.test.phone.newflights.FlightTestHelpers;
import com.expedia.bookings.test.pagemodels.flights.FlightsResultsScreen;
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;
import com.expedia.bookings.test.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;

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
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;
import static com.expedia.bookings.test.espresso.CustomMatchers.withTextColor;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsCompletelyDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsNotDisplayed;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class NewFlightPhoneHappyPathTest extends NewFlightTestCase {

	@Override
	public void runTest() throws Throwable {
		Method method = getClass().getMethod(getName(), (Class[]) null);
		if (method.getName().equals("testNewFlightHappyPath") || method.getName()
			.equals("testNewFlightHappyPathWithMaterialForms")) {
			Intents.init();
			intending(hasComponent(WebViewActivity.class.getName()))
				.respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
		}
		super.runTest();
	}

	@Override
	protected void tearDown() throws Exception {
		Method method = getClass().getMethod(getName(), (Class[]) null);

		if (method.getName().equals("testNewFlightHappyPath") || method.getName().equals("testNewFlightHappyPathWithMaterialForms")) {
			Intents.release();
		}
		super.tearDown();
	}


	@Test
	public void testNewFlightHappyPathWithMaterialForms() throws Throwable {
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms);
		SearchScreen.origin().perform(click());
		SearchScreen.selectFlightOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);

		SearchScreen.searchButton().perform(click());

		FlightTestHelpers.assertFlightOutbound();
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 2);
		FlightsScreen.selectOutboundFlight().perform(click());

		FlightTestHelpers.assertFlightInbound();
		FlightTestHelpers.assertDockedOutboundFlightSelectionWidget();
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("Outbound");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("happy_round_trip_with_insurance_available");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("9:00 pm - 11:00 pm (2h 0m)");
		FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0);
		assertInsuranceVisibilityTests();
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
		PackageScreen.swipeToAddInsurance();
		assertInsuranceIsAdded();
		assertInsuranceToggleIsEnabled();
		PackageScreen.toggleInsuranceSwitch();
		assertInsuranceIsNotAdded();
		assertInsuranceToggleIsEnabled();

		PackageScreen.travelerInfo().perform(scrollTo(), click());

		onView(withId(R.id.first_name_input)).perform(scrollTo(), click());
		onView(withId(R.id.last_name_input)).perform(scrollTo(), click());
		onView(withText(R.string.first_name_validation_error_message)).check(matches(isDisplayed()));
		onView(withId(R.id.edit_email_address)).perform(scrollTo(), click());
		onView(withText(R.string.last_name_validation_error_message)).check(matches(isDisplayed()));
		onView(withId(R.id.edit_phone_number)).perform(scrollTo(), click());
		onView(withText(R.string.email_validation_error_message)).check(matches(isDisplayed()));
		onView(withId(R.id.middle_name_input)).perform(scrollTo(), click());
		Espresso.closeSoftKeyboard();
		onView(withText(R.string.phone_validation_error_message)).check(matches(isDisplayed()));
		Espresso.pressBack();

		onView(withText("Enter missing traveler details")).check(doesNotExist());
		PackageScreen.travelerInfo().perform(scrollTo(), click());
		onView(withText(R.string.first_name_validation_error_message)).check(doesNotExist());
		onView(withText(R.string.last_name_validation_error_message)).check(doesNotExist());
		onView(withText(R.string.email_validation_error_message)).check(doesNotExist());
		onView(withText(R.string.phone_validation_error_message)).check(doesNotExist());
		Espresso.pressBack();

		PackageScreen.travelerInfo().perform(scrollTo(), click());

		onView(withId(R.id.material_edit_phone_number_country_code)).check(matches(withCompoundDrawable(R.drawable.material_dropdown)));
		onView(withId(R.id.edit_gender_btn)).check(matches(withCompoundDrawable(R.drawable.material_dropdown)));
		onView(withId(R.id.edit_birth_date_text_btn)).check(matches(withCompoundDrawable(R.drawable.material_dropdown)));
		TravelerDetails.enterFirstName("Eidur");
		TravelerDetails.enterLastName("Gudjohnsen");
		TravelerDetails.enterEmail("test@gmail.com");
		Espresso.closeSoftKeyboard();
		TravelerDetails.enterPhoneNumber("4155554321");
		Espresso.closeSoftKeyboard();
		TravelerDetails.selectBirthDate(1989, 6, 9);
		TravelerDetails.materialSelectGender("Male");
		TravelerDetails.clickAdvanced();
		TravelerDetails.enterRedressNumber("1234567");
		onView(withId(R.id.contact_airline_text)).perform(scrollTo()).check(matches(isDisplayed()));
		TravelerDetails.clickDone();

		PackageScreen.clickPaymentInfo();
		onView(withId(R.id.edit_creditcard_exp_text_btn)).check(matches(withCompoundDrawable(R.drawable.material_dropdown)));
		onView(withId(R.id.material_edit_country)).check(matches(withCompoundDrawable(R.drawable.material_dropdown)));
		PackageScreen.enterCreditCard();
		PackageScreen.completePaymentForm();
		assertInsuranceIsNotVisible();
		PackageScreen.clickPaymentDone();
//		TODO Reenable this flaky part of the test
// 		PackageScreen.clickLegalInformation();
//
//		assertLegalInformation();
//		Common.pressBack();

		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.DefaultVariant.CONTROL.ordinal());
	}

	@Test
	public void testNewFlightHappyPath() throws Throwable {
		getToCheckoutScreen();
		assertViewIsCompletelyDisplayed(R.id.checkout_button);
		onView(withId(R.id.checkout_button)).check(matches(withTextColor("#30FFFFFF")));
		assertCostSummaryView();
		PackageScreen.travelerInfo().perform(scrollTo(), click());
		assertViewIsNotDisplayed(R.id.checkout_button);
		Espresso.closeSoftKeyboard();
		Common.pressBack();
		assertViewIsCompletelyDisplayed(R.id.checkout_button);
		CheckoutViewModel.signInOnCheckout();
		EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(R.id.login_widget), 10, TimeUnit.SECONDS);
		assertViewIsDisplayed(R.id.slide_to_purchase_widget);
		assertViewIsNotDisplayed(R.id.checkout_button);
	}

	@Test
	public void testNewFlightHappyPathSignedIn() throws Throwable {
		getToCheckoutScreen();
		CheckoutViewModel.signInOnCheckout();
		EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(R.id.login_widget), 10, TimeUnit.SECONDS);

		PackageScreen.travelerInfo().perform(scrollTo(), click());
		Espresso.closeSoftKeyboard();
		TravelerDetails.clickAdvanced();
		onView(withId(R.id.traveler_number)).check(matches(withText("TN123456789")));
		onView(withId(R.id.redress_number)).check(matches(withText("1234567")));
		TravelerDetails.clickDone();

		//This is needed for 6.0+ devices. So far we don't know why this happens only under automated test run,
		// and it's a low enough priority where additional time might not be spent to fix this.
		SaveTravelerPrompt.ifPresentClickSave();

		CheckoutViewModel.clickPaymentInfo();
		CheckoutViewModel.selectStoredCard("Saved AmexTesting");
		Common.pressBack();
		CheckoutViewModel.performSlideToPurchase(true);
		assertSignedInConfirmationView();
	}

	private void getToCheckoutScreen() throws Throwable {
		selectOriginDestinationAndDates();

		SearchScreen.searchButton().perform(click());
		selectFirstOutboundFlight();
		selectFirstInboundFlight();

		assertCheckoutOverview();
		assertCostSummaryView();
		onView(withId(R.id.checkout_button)).check(matches(withTextColor("#FFFFFFFF")));

		PackageScreen.checkout().perform(click());

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
		onView(withId(R.id.rules_and_restrictions)).perform(ViewActions.waitForViewToDisplay());
		onView(withId(R.id.terms_and_conditions)).check(matches(isDisplayed()));
		onView(withId(R.id.privacy_policy)).check(matches(isDisplayed()));
		onView(withId(R.id.liabilities_link_text_view)).check(matches(isDisplayed()));
	}

	private void assertConfirmationView() {

		onView(withId(R.id.confirmation_container)).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.destination),
			isDescendantOfA(withId(R.id.confirmation_container)))).check(
			matches(withText("Detroit")));

		onView(allOf(withId(R.id.confirmation_title),
			isDescendantOfA(withId(R.id.outbound_flight_card)))).check(
			matches(withText("SFO to DTW")));

		onView(allOf(withId(R.id.confirmation_title),
			isDescendantOfA(withId(R.id.inbound_flight_card)))).check(
			matches(withText("DTW to SFO")));

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

		onView(allOf(withId(R.id.confirmation_title),
			isDescendantOfA(withId(R.id.outbound_flight_card)))).check(
			matches(withText("SFO to DTW")));

		onView(allOf(withId(R.id.confirmation_title),
			isDescendantOfA(withId(R.id.inbound_flight_card)))).check(
			matches(withText("DTW to SFO")));

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
			withText("San Francisco, CA"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.travelers), withParent(withId(R.id.checkout_overview_floating_toolbar)),
			withText("1 traveler"))).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.flight_card_view_text),
			isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)))).check(
			matches(withText("Flight to (SFO) Detroit")));

		onView(allOf(withId(R.id.flight_card_view_text),
			isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)))).check(
			matches(withText("Flight to (happy) San Francisco")));

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

		PackageScreen.bundleTotalFooterWidget().check((matches(withContentDescription(
			"Trip total is $696. Includes taxes and fees. . Cost Breakdown dialog. Button."))));
	}

	private void assertInsuranceVisibilityTests() {
		// insurance visibility in flight summary screen (Abacus 12268)
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppOfferInsuranceInFlightSummary,
			AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		FlightsScreen.selectInboundFlight().perform(click());
		assertInsuranceIsVisible();
		PackageScreen.checkout().perform(click());
		assertInsuranceIsNotVisible();
		Common.pressBack();
		Common.pressBack();

		// insurance visibility in checkout screen (control)
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppOfferInsuranceInFlightSummary,
			AbacusUtils.DefaultVariant.CONTROL.ordinal());
		FlightsScreen.selectInboundFlight().perform(click());
		assertInsuranceIsNotVisible();
		PackageScreen.checkout().perform(click());
		assertInsuranceIsVisible();
		Common.pressBack();
		Common.pressBack();
	}

	private void assertInsuranceBenefits() {
		onView(withId(R.id.insurance_benefits_dialog_body)).check(matches(isDisplayed()))
			.perform(pressBack()).check(doesNotExist());
	}

	private void assertInsuranceIsAdded() {
		PackageScreen.showPriceBreakdown();
		onView(withText(R.string.cost_summary_breakdown_flight_insurance)).check(matches(isDisplayed()));
		Espresso.pressBack();
		onView(withId(R.id.bundle_total_price)).check(matches(withText("$715")));
		onView(allOf(withId(R.id.insurance_title), isDisplayed())).check(matches(
			withText("Your trip is protected for $19/person")));
	}

	private void assertInsuranceIsNotAdded() {
		PackageScreen.showPriceBreakdown();
		onView(withText(R.string.cost_summary_breakdown_flight_insurance)).check(doesNotExist());
		Espresso.pressBack();
		onView(withId(R.id.bundle_total_price)).check(matches(withText("$696")));
		onView(allOf(withId(R.id.insurance_title), isDisplayed())).check(matches(
			withText("Add protection for $19/person")));
	}

	private void assertInsuranceIsNotVisible() {
		onView(allOf(withId(R.id.insurance_widget), isDisplayed())).check(doesNotExist());
	}

	private void assertInsuranceIsVisible() {
		onView(allOf(withId(R.id.insurance_widget), isDisplayed())).check(matches(isDisplayed()));
	}

	private void assertInsuranceToggleIsEnabled() {
		onView(allOf(withId(R.id.insurance_switch), isDisplayed())).check(matches(isEnabled()));
	}

	private void assertInsuranceTerms() {
		intended(hasComponent(WebViewActivity.class.getName()));
	}

}
