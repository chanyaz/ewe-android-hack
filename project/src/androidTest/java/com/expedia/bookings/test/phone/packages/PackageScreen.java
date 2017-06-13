package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class PackageScreen {

	public static ViewInteraction errorDialog(String text) {
		return onView(withText(text))
			.inRoot(withDecorView(not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))));
	}

	public static ViewInteraction showMoreButton() {
		return onView(withId(R.id.collapsed_container));
	}

	public static ViewInteraction hotelResultsHeader() {
		return onView(withId(R.id.results_description_header));
	}

	public static ViewInteraction searchToolbar() {
		return onView(withId(R.id.search_toolbar));
	}

	public static ViewInteraction hotelResultsToolbar() {
		return onView(withId(R.id.hotel_results_toolbar));
	}

	public static ViewInteraction hotelDetailsToolbar() {
		return onView(withId(R.id.hotel_details_toolbar));
	}

	public static ViewInteraction hotelCardViewOnBundleOverviewText() {
		return onView(withId(R.id.hotels_card_view_text));
	}

	public static ViewInteraction hotelTravelerDatesOnBundleOverviewText() {
		return onView(withId(R.id.hotels_dates_guest_info_text));
	}



	public static void searchPackage() throws Throwable {
		SearchScreen.doGenericSearch();
	}

	public static void searchPackageFor(int adults, int children) throws Throwable {
		SearchScreen.search(adults, children, false, false);
	}

	public static ViewInteraction bundleToolbar() {
		return onView(withId(R.id.checkout_toolbar));
	}

	public static ViewInteraction toolbarNavigationUp(int id) {
		return onView(allOf(withParent(withId(id)), withClassName(is(AppCompatImageButton.class.getName()))));
	}

	public static ViewInteraction hotelBundleWidget() {
		return onView(withId(R.id.bundle_widget));
	}

	public static ViewInteraction hotelBundle() {
		return onView(withId(R.id.package_bundle_hotel_widget));
	}

	public static ViewInteraction hotelDatesRoomInfo() {
		return onView(withId(R.id.hotels_dates_guest_info_text));
	}

	public static ViewInteraction outboundFlightCardInfo() {
		return outboundFlightDescendant(withId(R.id.travel_info_view_text));
	}

	private static ViewInteraction outboundFlightDescendant(Matcher<View> descendantViewMatcher) {
		return onView(
			allOf(descendantViewMatcher, isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))));
	}

	public static ViewInteraction inboundFlightCardInfo() {
		return inboundFlightDescendant(withId(R.id.travel_info_view_text));
	}

	private static ViewInteraction inboundFlightDescendant(Matcher<View> descendantViewMatcher) {
		return onView(allOf(descendantViewMatcher, isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))));
	}

	public static ViewInteraction flightsToolbar() {
		return onView(withId(R.id.flights_toolbar));
	}

	public static ViewInteraction flightsToolbarSearchMenu() {
		return onView(withId(R.id.menu_search));
	}

	public static ViewInteraction flightsToolbarFilterMenu() {
		return onView(withId(R.id.filter_btn));
	}

	public static ViewInteraction outboundFlight() {
		return onView(withId(R.id.package_bundle_outbound_flight_widget));
	}

	public static ViewInteraction hotelInfo() {
		return onView(allOf(isDescendantOfA(withId(R.id.package_bundle_hotel_widget)),
			withId(R.id.hotel_info_container)));
	}

	public static ViewInteraction hotelDetailsIcon() {
		return onView(allOf(isDescendantOfA(withId(R.id.package_bundle_hotel_widget)),
			withId(R.id.package_hotel_details_icon)));
	}

	public static ViewInteraction outboundFlightInfo() {
		return onView(allOf(isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)),
			withId(R.id.flight_info_container)));
	}

	public static ViewInteraction outboundFlightInfoRowContainer() {
		return onView(allOf(isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)),
			withId(R.id.row_container)));
	}

	public static ViewInteraction inboundFlightInfo() {
		return onView(allOf(isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)),
			withId(R.id.flight_info_container)));
	}

	public static ViewInteraction inboundFlightInfoRowContainer() {
		return onView(allOf(isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)),
			withId(R.id.row_container)));
	}

	public static ViewInteraction inboundFLight() {
		return onView(withId(R.id.package_bundle_inbound_flight_widget));
	}

	public static ViewInteraction outboundFlightDetailsIcon() {
		return outboundFlightDescendant(withId(R.id.package_flight_details_icon));
	}

	public static ViewInteraction outboundFlightDetailsContainer() {
		return outboundFlightDescendant(withId(R.id.flight_details_container));
	}

	public static ViewInteraction inboundFlightDetailsContainer() {
		return inboundFlightDescendant(withId(R.id.flight_details_container));
	}

	public static ViewInteraction flightList() {
		return onView(withId(R.id.list_view));
	}

	public static ViewInteraction flightFilterView() {
		return onView(withId(R.id.filter_container));
	}

	public static ViewInteraction selectFlight(int index) {
		flightList().perform(waitForViewToDisplay());
		int adjustPosition = 3;
		return flightList().perform(RecyclerViewActions.actionOnItemAtPosition(index + adjustPosition, click()));
	}

	public static ViewInteraction selectThisFlight() {
		return onView(withId(R.id.select_flight_button));
	}

	public static void clickHotelBundle() {
		PackageScreen.hotelBundle().perform(waitForViewToDisplay());
		PackageScreen.hotelBundle().perform(click());
	}

	public static ViewInteraction baggageFeeInfo() {
		return onView(withId(R.id.show_baggage_fees));
	}

	public static ViewInteraction checkout() {
		return onView(withId(R.id.checkout_button));
	}

	public static ViewInteraction travelerInfo() {
		return onView(withId(R.id.traveler_default_state));
	}

	public static ViewInteraction travelerAdvance() {
		return onView(withId(R.id.traveler_advanced_options_button));
	}

	public static ViewInteraction bundlePriceWidget() {
		return onView(withId(R.id.bundle_price_widget));
	}

	public static ViewInteraction resultsHeader() {
		return onView(withId(R.id.results_description_header));
	}

	public static ViewInteraction slidingBundleWidget() {
		return onView(withId(R.id.sliding_bundle_widget));
	}

	public static ViewInteraction hotelRoomImageView() {
		return onView(withId(R.id.selected_hotel_room_image));
	}

	public static ViewInteraction hotelBundleContainer() {
		return onView(allOf(isDescendantOfA(withId(R.id.package_bundle_hotel_widget)), withId(R.id.row_container)));
	}

	public static ViewInteraction outboundFlightBundleContainer() {
		return onView(allOf(isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)), withId(R.id.row_container)));
	}

	public static ViewInteraction inboundFlightBundleContainer() {
		return onView(allOf(isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)), withId(R.id.row_container)));
	}

	public static ViewInteraction bundleTotalFooterWidget() {
		return onView(withId(R.id.total_price_widget));
	}

	public static ViewInteraction bundleTotalSlidingWidget() {
		return onView(withId(R.id.bundle_price_widget));
	}

	public static void tickCheckboxWithText(String title) {
		checkBoxContainerWithTitle(title).perform(scrollTo());
		checkBoxContainerWithTitle(title).perform(click());
	}

	public static ViewInteraction checkBoxWithTitle(String title) {
		return onView(CoreMatchers.allOf(withId(R.id.check_box), hasSibling(CoreMatchers.allOf(withId(R.id.label), withText(title)))));
	}

	public static ViewInteraction checkBoxContainerWithTitle(String title) {
		return onView(CoreMatchers.allOf(withId(R.id.filter_categories_widget), hasDescendant(CoreMatchers.allOf(withId(R.id.label), withText(title)))));
	}

	public static ViewInteraction stickySelectRoom() {
		return onView(
			allOf(withId(R.id.sticky_bottom_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static ViewInteraction addRoom() {
		return onView(
			allOf(
				withId(R.id.hotel_book_button),
				isDescendantOfA(allOf(withId(R.id.collapsed_container))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static void resetFlightsFliter() {
		onView(withId(R.id.dynamic_feedback_clear_button)).perform(click());
		EspressoUtils.assertViewIsNotDisplayed(R.id.dynamic_feedback_container);
	}

	public static void clickAddRoom() {
		waitForDetailsLoaded();
		addRoom().perform(click());
	}

	public static void selectRoom() throws Throwable {
		clickAddRoom();
		Common.delay(2);
	}

	public static void selectFirstRoom() {
		waitForDetailsLoaded();
		stickySelectRoom().perform(click());
		addRoom().perform(click());
	}

	public static void waitForDetailsLoaded() {
		onView(withId(R.id.hotel_detail)).perform(waitForViewToDisplay());
	}

	public static void enterTravelerInfo() {
		travelerInfo().perform(scrollTo(), click());
		enterFirstName("FiveStar");
		enterLastName("Bear");
		enterEmail("noah@mobiata.com");
		Espresso.closeSoftKeyboard();
		enterPhoneNumber("7732025862");
		Espresso.closeSoftKeyboard();
		selectBirthDate(9, 6, 1989);
		selectGender("Male");

		clickTravelerAdvanced();
		enterRedressNumber("1234567");

		clickTravelerDone();
	}

	// TODO Probably want to move these methods somewhere else.
	public static void enterFirstName(String name) {
		onView(withId(R.id.first_name_input)).perform(typeText(name));
	}

	public static void enterLastName(String name) {
		onView(withId(R.id.last_name_input)).perform(typeText(name));
	}

	public static void enterRedressNumber(String redressNumber) {
		onView(withId(R.id.redress_number)).perform(scrollTo(), typeText(redressNumber));
	}

	public static void enterKnownTravelerNumber(String knownTravelerNumber) {
		onView(withId(R.id.traveler_number)).perform(typeText(knownTravelerNumber));
	}

	public static void enterPhoneNumber(String phoneNumber) {
		onView(withId(R.id.edit_phone_number)).perform(typeText(phoneNumber));
	}

	public static void enterEmail(String email) {
		onView(withId(R.id.edit_email_address)).perform(typeText(email));
	}

	public static void selectGender(String genderType) {
		onView(withId(R.id.edit_gender_spinner)).perform(click());
		onData(allOf(is(instanceOf(String.class)),is(genderType))).perform(click());
	}

	public static void materialSelectGender(String genderType) {
		onView(withId(R.id.edit_gender_btn)).perform(click());
		onView(withText(genderType)).perform(click());
	}

	public static void selectBirthDate(int year, int month, int day) {
		onView(withId(R.id.edit_birth_date_text_btn)).perform(click());
		Espresso.closeSoftKeyboard();
		onView(withId(R.id.datePicker)).perform(ViewActions.waitForViewToDisplay());
		onView(withId(R.id.datePicker)).perform(PickerActions.setDate(year, month, day));
		onView(withId(R.id.datePickerDoneButton)).perform(click());
	}

	public static void clickTravelerDone() {
		onView(withId(R.id.menu_done)).perform(ViewActions.waitForViewToDisplay());
		onView(withId(R.id.menu_done)).perform(click());
	}

	public static void clickTravelerAdvanced() {
		onView(withId(R.id.traveler_advanced_options_button)).perform(scrollTo());
		onView(withId(R.id.traveler_advanced_options_button)).perform(click());
	}

	public static void closeDateErrorDialog() {
		onView(withId(android.R.id.button1)).perform(waitForViewToDisplay());
		onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
	}

	public static void clickLegalInformation() {
		onView(withId(R.id.bottom_container)).perform(waitForViewToDisplay());
		onView(withId(R.id.legal_information_text_view)).perform(scrollTo());
		onView(withId(R.id.legal_information_text_view)).perform(waitForViewToDisplay(), click());
	}

	public static void clickPaymentDone() {
		CheckoutViewModel.clickDone();
	}

	public static void clickPaymentInfo() {
		CheckoutViewModel.waitForPaymentInfoDisplayed();
		CheckoutViewModel.clickPaymentInfo();
	}

	public static void clickSpecialAssistance() {
		onView(withId(R.id.edit_assistance_preference_spinner)).perform(click());
	}

	public static void clickSeatPreference() {
		onView(withId(R.id.edit_seat_preference_spinner)).perform(click());
	}

	public static void enterCreditCard() {
		CardInfoScreen.creditCardNumberEditText().perform(waitForViewToDisplay());
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
	}

	public static void completePaymentForm() {
		completeCardInfo("Mobiata Auto");
	}

	private static void completeCardInfo(String nameOnCard) {
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextCvv("666");
		CardInfoScreen.typeTextNameOnCardEditText(nameOnCard);

		int addressSectionParentId = R.id.section_location_address;
		BillingAddressScreen.typeTextAddressLineOne("123 California Street", addressSectionParentId);
		BillingAddressScreen.typeTextCity("San Francisco", addressSectionParentId);
		BillingAddressScreen.typeTextState("CA", addressSectionParentId);
		BillingAddressScreen.typeTextPostalCode("94105", addressSectionParentId);
	}

	public static void enterPaymentInfo() {
		clickPaymentInfo();
		enterCreditCard();
		completePaymentForm();
		clickPaymentDone();
	}

	public static String getDatesGuestInfoText(LocalDate startDate, LocalDate endDate) {
		StringBuilder sb = new StringBuilder(DateUtils.localDateToMMMd(startDate));
		sb.append(" - ").append(DateUtils.localDateToMMMd(endDate));
		sb.append(", 1 Guest");
		return sb.toString();
	}

	public static void assertErrorScreen(String buttonText, String errorText) {
		onView(AllOf.allOf(withId(R.id.error_action_button), withText(buttonText))).check(matches(isDisplayed()));
		onView(AllOf.allOf(withId(R.id.error_text), withText(errorText))).check(matches(isDisplayed()));
		onView(withId(R.id.error_image)).check(matches(isDisplayed()));
	}

	public static ViewInteraction bundleOverviewHotelRowContainer() {
		return onView(allOf(withId(R.id.row_container), isDescendantOfA(withId(R.id.package_bundle_hotel_widget))));
	}

	public static void doPackageSearch() throws Throwable {
		searchPackage();
		HotelScreen.selectHotel("Package Happy Path");
		HotelScreen.selectFirstRoom();

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(waitForViewToDisplay(), click());

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(waitForViewToDisplay(), click());
	}

	public static void checkFlightToolBarMenuItemsVisibility(boolean isVisible) {
		PackageScreen.flightsToolbarSearchMenu().check(doesNotExist());

		if (isVisible) {
			PackageScreen.flightsToolbarFilterMenu().check(matches(isDisplayed()));
		}
		else {
			PackageScreen.flightsToolbarFilterMenu().check(doesNotExist());
		}
	}

	public static ViewInteraction showInsuranceBenefits() {
		return onView(allOf(withId(R.id.insurance_description), isDisplayed())).perform(click());
	}

	public static ViewInteraction showInsuranceTerms() {
		return onView(allOf(withId(R.id.insurance_terms), isDisplayed())).perform(click());
	}

	public static ViewInteraction showPriceBreakdown() {
		return onView(withId(R.id.bundle_total_text)).perform(click());
	}

	public static ViewInteraction toggleInsuranceSwitch() {
		return onView(allOf(withId(R.id.insurance_switch), isDisplayed())).perform(click());
	}

	public static ViewInteraction swipeToAddInsurance() {
		return onView(allOf(withId(R.id.insurance_switch), isDisplayed(), isNotChecked())).perform(swipeRight());
	}

	public static void enterPaymentInfo(@NotNull String nameOnCard) {
		clickPaymentInfo();
		enterCreditCard();
		completeCardInfo(nameOnCard);
		clickPaymentDone();
	}
}
