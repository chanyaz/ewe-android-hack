package com.expedia.bookings.test.phone.rail;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.CalendarPickerActions;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.allOf;

public class RailScreen {

	public static ViewInteraction calendarButton() {
		return onView(withId(R.id.calendar_card));
	}

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(CalendarPickerActions.clickDates(start, end));
	}

	public static void selectOneWay() {
		onView(withText(R.string.rail_one_way)).perform(click());
	}

	public static void selectRoundTrip() {
		onView(withText(R.string.rail_return)).perform(click());
	}

	public static ViewInteraction dialogDoneButton() {
		return onView(withId(android.R.id.button1));
	}

	public static void scrollToOutboundFareOptions() {
		scrollToIdWithGrandParent(R.id.details_fare_options, R.id.rail_outbound_details_presenter);
	}

	public static void scrollToInboundFareOptions() {
		scrollToIdWithGrandParent(R.id.details_fare_options, R.id.rail_inbound_details_presenter);
	}

	private static void scrollToIdWithGrandParent(int targetId, int grandParentViewId) {
		onView(allOf(withId(targetId),
			isDescendantOfA(withId(grandParentViewId)))).perform(scrollTo());
	}

	public static ViewInteraction selectFareOption(String fareOption) {
		return onView(
			allOf(
				withId(R.id.select_button), withText(R.string.select),
				hasSibling(allOf(withId(R.id.price_container), withChild(withText(fareOption)))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static void clickSelectFareOption(String amount) {
		selectFareOption(amount).perform(scrollTo(), click());
	}

	public static void clickAmenitiesLink(String fareClass) {
		selectAmenitiesLink(fareClass).perform(scrollTo(), click());
	}

	public static ViewInteraction selectAmenitiesLink(String fareClass) {
		return onView(
			allOf(
				withId(R.id.amenities_link), allOf(withText(R.string.amenities)),
				isDescendantOfA(allOf(withId(R.id.details_fare_options))),
				hasSibling(allOf(withId(R.id.fare_description), withText(fareClass))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static void clickFareRules(String fareType, String fareDesc) {
		onView(allOf(withText(fareType), hasSibling(withText(fareDesc)))).perform(scrollTo(), click());
	}

	public static ViewInteraction checkoutButton() {
		return onView(withId(R.id.checkout_button));
	}

	public static void clickTravelerCard() {
		onView(withId(R.id.rail_traveler_card_view)).perform(click());
	}

	public static void fillInTraveler() {
		onView(withId(R.id.rail_traveler_toolbar)).perform(waitForViewToDisplay());
		enterFirstName("FiveStar");
		enterLastName("Bear");
		enterEmail("noah@mobiata.com");
		Espresso.closeSoftKeyboard();
		enterPhoneNumber("7732025862");

		clickToolbarDone(R.id.rail_traveler_toolbar);
	}

	public static void enterFirstName(String name) {
		onView(withId(R.id.first_name_input)).perform(typeText(name));
	}

	public static void enterLastName(String name) {
		onView(withId(R.id.last_name_input)).perform(typeText(name));
	}

	public static void enterPhoneNumber(String phoneNumber) {
		onView(withId(R.id.edit_phone_number)).perform(typeText(phoneNumber));
	}

	public static void enterEmail(String email) {
		onView(withId(R.id.edit_email_address)).perform(typeText(email));
	}

	public static ViewInteraction outboundLegInfo() {
		return onView(allOf(isDescendantOfA(withId(R.id.rail_outbound_leg_widget)), withId(R.id.rail_leg_container)));
	}

	public static ViewInteraction outboundDetailsIcon() {
		return onView(allOf(isDescendantOfA(withId(R.id.rail_outbound_leg_widget)), withId(R.id.rail_leg_details_icon)));
	}

	public static ViewInteraction ouboundFareDescriptionInfo() {
		return onView(allOf(isDescendantOfA(withId(R.id.rail_outbound_leg_widget)), withId(R.id.fare_description)));
	}

	public static void navigateToDetails() throws Throwable {
		SearchScreen.selectRailOriginAndDestination();
		RailScreen.calendarButton().perform(click());
		LocalDate startDate = LocalDate.now().plusDays(10);
		RailScreen.selectDates(startDate, null);
		RailScreen.dialogDoneButton().perform(click());

		SearchScreen.searchButton().perform(click());
		onView(withId(R.id.rail_outbound_list)).perform(scrollToPosition(5));
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(onView(withId(R.id.rail_outbound_list)), 5, R.id.timesView,
			"12:55 PM – 4:16 PM");
		onView(withId(R.id.rail_outbound_list)).perform(RecyclerViewActions.actionOnItemAtPosition(5, click()));
		onView(allOf(withText("London Underground"),
			isDescendantOfA(withId(R.id.details_timeline)))).check(matches(isDisplayed()));
	}

	public static void performRoundTripSearch() throws Throwable {
		selectRoundTrip();
		SearchScreen.selectRailOriginAndDestination();
		selectRoundTripDates();
		SearchScreen.searchButton().perform(click());
	}

	public static void selectRoundTripOutbound() {
		onView(withId(R.id.rail_outbound_list)).perform(scrollToPosition(3));
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(onView(withId(R.id.rail_outbound_list)), 3, R.id.timesView,
			"8:30 AM – 12:37 PM");
		onView(withId(R.id.rail_outbound_list)).perform(RecyclerViewActions.actionOnItemAtPosition(3, click()));

		scrollToOutboundFareOptions();
		onView(withText("Standard Anytime Day Single")).check(matches(isDisplayed()));
		clickSelectFareOption("£30.00");
	}

	public static void selectRoundTripInbound() {
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(onView(withId(R.id.rail_inbound_list)), 1, R.id.timesView,
			"12:52 PM – 5:14 PM");
		onView(withId(R.id.rail_inbound_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
		scrollToInboundFareOptions();
		clickSelectFareOption("+£8.20");
	}

	public static void checkoutAndPurchase() {
		checkoutButton().perform(click());

		clickTravelerCard();
		fillInTraveler();
		onView(withId(R.id.rail_traveler_card_view)).perform(waitForViewToDisplay());
		onView(withId(R.id.rail_traveler_card_view)).check(matches(isDisplayed()));

		CheckoutViewModel.waitForPaymentInfoDisplayed();
		CheckoutViewModel.paymentInfo().perform(click());
		enterPaymentDetails();

		performSlideToPurchase();
	}

	public static void selectRoundTripDates() {
		calendarButton().perform(click());

		DateTime startDateTime = DateTime.now().plusDays(3).withTimeAtStartOfDay();
		LocalDate startDate = startDateTime.toLocalDate();
		String expectedStartDateTime = DateUtils.dateTimeToMMMdhmma(startDateTime);

		DateTime endDateTime = startDateTime.plusDays(1).withTimeAtStartOfDay();
		LocalDate endDate = endDateTime.toLocalDate();
		String expectedEndDateTime = DateUtils.dateTimeToMMMdhmma(endDateTime);
		selectDates(startDate, endDate);

		EspressoUtils.assertViewIsDisplayed(R.id.depart_slider_container);
		EspressoUtils.assertViewIsDisplayed(R.id.return_slider_container);
		dialogDoneButton().perform(click());

		EspressoUtils.assertViewWithTextIsDisplayed(expectedStartDateTime + " – " + expectedEndDateTime);
	}

	public static void navigateToTripOverview() throws Throwable {
		navigateToDetails();

		RailScreen.scrollToOutboundFareOptions();
		onView(withText("Any off-peak train")).check(matches(isDisplayed()));
		RailScreen.clickSelectFareOption("£617.20");

		onView(withText("Outbound - Thu Nov 10")).perform(ViewActions.waitForViewToDisplay())
			.check(matches(isDisplayed()));
	}

	public static void enterPaymentDetails() {
		CardInfoScreen.creditCardNumberEditText().perform(waitForViewToDisplay());
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		CardInfoScreen.assertPaymentFormCardFeeWarningShown("Credit card fees: £2.90");

		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextCvv("666");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");

		int addressSectionParentId = R.id.section_location_address;
		BillingAddressScreen.typeTextAddressLineOne("123 California Street", addressSectionParentId);
		BillingAddressScreen.typeTextCity("San Francisco", addressSectionParentId);
		BillingAddressScreen.typeTextState("CA", addressSectionParentId);
		BillingAddressScreen.typeTextPostalCode("94105", addressSectionParentId);

		clickToolbarDone(R.id.rail_checkout_toolbar);
	}

	public static void performSlideToPurchase() {
		onView(withId(R.id.rail_slide_to_purchase_widget)).perform(ViewActions.waitForViewToDisplay());
		onView(withId(R.id.rail_slide_to_purchase_widget)).perform(ViewActions.swipeRight());
	}

	private static void clickToolbarDone(int toolbarId) {
		onView(allOf(isDescendantOfA(withId(toolbarId)), withId(R.id.menu_done)))
			.perform(ViewActions.waitForViewToDisplay());
		onView(allOf(isDescendantOfA(withId(toolbarId)), withId(R.id.menu_done))).perform(click());
	}
}
