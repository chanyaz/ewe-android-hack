package com.expedia.bookings.test.pagemodels.packages;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
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
		SearchScreenActions.search(1, 0, false, false);
	}

	public static void searchPackageFor(int adults, int children) throws Throwable {
		SearchScreenActions.search(adults, children, false, false);
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
		return listView();
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
		return onView(allOf(isDescendantOfA(withId(R.id.package_bundle_hotel_widget)),
			withId(R.id.row_container)));
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

	public static void resetFlightsFliter() {
		onView(withId(R.id.dynamic_feedback_clear_button)).perform(click());
		EspressoUtils.assertViewIsNotDisplayed(R.id.dynamic_feedback_container);
	}

	public static void waitForDetailsLoaded() {
		onView(withId(R.id.hotel_detail)).perform(waitForViewToDisplay());
	}

	public static void enterTravelerInfo() {
		travelerInfo().perform(scrollTo(), click());
		TravelerDetails.enterFirstName("FiveStar");
		TravelerDetails.enterLastName("Bear");
		TravelerDetails.enterEmail("noah@mobiata.com");
		Espresso.closeSoftKeyboard();
		TravelerDetails.enterPhoneNumber("7732025862");
		Espresso.closeSoftKeyboard();
		TravelerDetails.selectBirthDate(9, 6, 1989);
		TravelerDetails.materialSelectGender("Male");

		TravelerDetails.clickAdvanced();
		TravelerDetails.enterRedressNumber("1234567");

		TravelerDetails.clickDone();
	}

	// TODO Probably want to move these methods somewhere else.
	public static void closeDateErrorDialog() {
		onView(withId(android.R.id.button1)).perform(waitForViewToDisplay());
		onView(withId(android.R.id.button1)).perform(click());
	}

	public static void clickLegalInformation() {
		onView(withId(R.id.bottom_container)).perform(waitForViewToDisplay());
		onView(withId(R.id.legal_information_text_view)).perform(scrollTo());
		onView(withId(R.id.legal_information_text_view)).perform(waitForViewToDisplay(), scrollTo(), click());
	}

	public static void clickPaymentDone() {
		CheckoutScreen.clickDone();
	}

	public static void clickPaymentInfo() {
		CheckoutScreen.waitForPaymentInfoDisplayed();
		CheckoutScreen.clickPaymentInfo();
	}

	public static void clickSpecialAssistance() {
		onView(withId(R.id.edit_assistance_preference_button)).perform(click());
	}

	public static void clickSeatPreference() {
		onView(withId(R.id.edit_seat_preference_button)).perform(click());
	}

	public static void enterCreditCard() {
		CardInfoScreen.creditCardNumberEditText().perform(waitForViewToDisplay());
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
	}

	public static void enterCreditCardNumber(String cardNumb) throws Throwable {
		CardInfoScreen.creditCardNumberEditText().perform(waitForViewToDisplay());
		CardInfoScreen.creditCardNumberEditText().perform(typeText(cardNumb));
	}

	public static void errorMessageWhenCardNotAccepted(String errorMess) throws Throwable {
		CardInfoScreen.errorMessageCardNotAccepted().perform(waitForViewToDisplay());
		CardInfoScreen.errorMessageCardNotAccepted().check(matches(withText(errorMess)));
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
		StringBuilder sb = new StringBuilder(LocaleBasedDateFormatUtils.localDateToMMMd(startDate));
		sb.append(" - ").append(LocaleBasedDateFormatUtils.localDateToMMMd(endDate));
		sb.append(", 1 guest");
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
		HotelResultsScreen.selectHotel("Package Happy Path");
		HotelInfoSiteScreen.bookFirstRoom();

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
		return onView(allOf(isDescendantOfA(withId(R.id.total_price_widget)), withId(R.id.bundle_total_text))).perform(click());
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

	public static ViewInteraction moreOptions() {
		return onView(withContentDescription("More options"));
	}

	public static ViewInteraction filterIcon() {
		return onView(withId(R.id.filter_placeholder_icon));
	}

	public static ViewInteraction title() {
		return onView(withId(R.id.title));
	}

	public static ViewInteraction listView() {
		return onView(withId(R.id.list_view));
	}

	public static ViewInteraction dockedOBTextView() {
		return onView(withId(R.id.outbound_label));
	}

	public static ViewInteraction selectFHCTab() {
		return Espresso.onView(allOf(withText("Hotel + Flight + Car"), isDescendantOfA(withId(R.id.tabs))))
			.perform(click());
	}

	public static ViewInteraction webView() {
		return onView(withId(R.id.root_content));
	}
}
