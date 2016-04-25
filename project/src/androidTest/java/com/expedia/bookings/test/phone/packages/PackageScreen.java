package com.expedia.bookings.test.phone.packages;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.view.View;
import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.espresso.TabletViewActions;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class PackageScreen {

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar))
			.inRoot(withDecorView(not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))));
	}

	public static ViewInteraction calendarCard() {
		return onView(withId(R.id.calendar_card));
	}

	public static ViewInteraction selectDateButton() {
		return onView(allOf(withId(R.id.input_label), withParent(withId(R.id.calendar_card))));
	}

	public static ViewInteraction selectGuestsButton() {
		return onView(withId(R.id.traveler_card));
	}

	public static void setGuests(int adults, int children) {
		//Minimum 1 ADT selected
		for (int i = 1; i < adults; i++) {
			incrementAdultsButton();
		}

		for (int i = 0; i < children; i++) {
			incrementChildrenButton();
		}
		searchAlertDialogDone().perform(click());
	}

	public static void incrementChildrenButton() {
		onView(withId(R.id.children_plus)).perform(click());
	}

	public static void incrementAdultsButton() {
		onView(withId(R.id.adults_plus)).perform(click());
	}

	public static ViewInteraction destination() {
		return onView(withId(R.id.destination_card));
	}

	public static ViewInteraction arrival() {
		return onView(withId(R.id.arrival_card));
	}

	public static ViewInteraction searchEditText() {
		return onView(withId(android.support.v7.appcompat.R.id.search_src_text));
	}

	public static ViewInteraction suggestionList() {
		return onView(withId(R.id.suggestion_list));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendarCard().perform(click());
		calendar().perform(TabletViewActions.clickDates(start, end));
		searchAlertDialogDone().perform(click());
	}

	public static ViewInteraction searchAlertDialogDone() {
		return onView(withId(android.R.id.button1));
	}

	public static void selectLocation(String hotel) throws Throwable {
		suggestionList().perform(ViewActions.waitForViewToDisplay());
		final Matcher<View> viewMatcher = hasDescendant(withText(hotel));

		suggestionList().perform(ViewActions.waitFor(viewMatcher, 10, TimeUnit.SECONDS));
		suggestionList().perform(RecyclerViewActions.actionOnItem(viewMatcher, click()));
	}

	public static ViewInteraction searchButton() {
		return onView(withId(R.id.search_button_v2));
	}

	public static ViewInteraction errorDialog(String text) {
		return onView(withText(text))
			.inRoot(withDecorView(not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))));
	}

	public static void seeHotelResults() {
		onView(withId(R.id.hotel_info_container)).perform(click());
	}

	public static ViewInteraction hotelResultsHeader() {
		return onView(withId(R.id.pricing_structure_header));
	}

	public static ViewInteraction hotelResultsToolbar() {
		return onView(withId(R.id.hotel_results_toolbar));
	}

	public static ViewInteraction hotelDetailsToolbar() {
		return onView(withId(R.id.hotel_details_toolbar));
	}

	public static void searchPackage() throws Throwable {
		search(1, 0);
	}

	public static void searchPackageFor(int adults, int children) throws Throwable {
		search(adults, children);
	}

	public static ViewInteraction bundleToolbar() {
		return onView(withId(R.id.checkout_toolbar));
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
		return onView(withId(R.id.menu_filter));
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

	public static ViewInteraction inboundFlightInfo() {
		return onView(allOf(isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)),
			withId(R.id.flight_info_container)));
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

	public static ViewInteraction itin() {
		return onView(withId(R.id.itin_number));
	}

	public static ViewInteraction hotelPriceWidget() {
		return onView(withId(R.id.bundle_price_widget));
	}

	public static ViewInteraction hotelRoomImageView() {
		return onView(withId(R.id.selected_hotel_room_image));
	}

	public static void enterTravelerInfo() {
		travelerInfo().perform(scrollTo(), click());
		enterFirstName("FiveStar");
		enterLastName("Bear");
		// TODO fix after adding email
		//CheckoutViewModel.enterEmail("noah@mobiata.com");
		//Common.closeSoftKeyboard(CheckoutViewModel.email());
		enterPhoneNumber("7732025862");
		selectBirthDate(9, 6, 1989);

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
		onView(withId(R.id.redress_number)).perform(typeText(redressNumber));
	}

	public static void enterPhoneNumber(String phoneNumber) {
		onView(withId(R.id.edit_phone_number)).perform(typeText(phoneNumber));
	}

	public static void selectBirthDate(int year, int month, int day) {
		onView(withId(R.id.edit_birth_date_text_btn)).perform(click());
		Espresso.closeSoftKeyboard();
		onView(withId(R.id.datePicker)).perform(ViewActions.waitForViewToDisplay());
		onView(withId(R.id.datePicker)).perform(PickerActions.setDate(year, month, day));
		onView(withId(R.id.datePickerDoneButton)).perform(ViewActions.waitForViewToDisplay());
		onView(withId(R.id.datePickerDoneButton)).perform(click());
	}

	public static void clickTravelerDone() {
		onView(withId(R.id.menu_done)).perform(ViewActions.waitForViewToDisplay());
		onView(withId(R.id.menu_done)).perform(click());
	}

	public static void clickTravelerAdvanced() {
		onView(withId(R.id.traveler_advanced_options_button)).perform(click());
	}

	public static void clickHotelBundleContainer() {
		onView(withId(R.id.row_container)).perform(click());
	}

	public static void enterPaymentInfo() {
		Common.delay(2);
		CheckoutViewModel.clickPaymentInfo();
		Common.delay(1);
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextCvv("666");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.typeTextEmailEditText("test@email.com");

		BillingAddressScreen.typeTextAddressLineOne("123 California Street");
		BillingAddressScreen.typeTextCity("San Francisco");
		BillingAddressScreen.typeTextState("CA");
		BillingAddressScreen.typeTextPostalCode("94105");
		CheckoutViewModel.clickDone();
		Common.delay(2);
	}

	private static void search(int adults, int children) throws Throwable {
		selectDepartureAndArrival();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		PackageScreen.selectDates(startDate, endDate);
		PackageScreen.selectGuestsButton().perform(click());
		PackageScreen.setGuests(adults, children);
		PackageScreen.searchButton().perform(click());
	}

	public static void selectDepartureAndArrival() throws Throwable {
		destination().perform(click());
		searchEditText().perform(typeText("SFO"));
		selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		arrival().perform(click());
		searchEditText().perform(typeText("DTW"));
		selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)");
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

}
