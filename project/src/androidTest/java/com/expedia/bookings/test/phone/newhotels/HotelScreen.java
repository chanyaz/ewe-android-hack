package com.expedia.bookings.test.phone.newhotels;

import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.espresso.TabletViewActions;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;
import static org.hamcrest.Matchers.allOf;

public class HotelScreen {

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static ViewInteraction selectDateButton() {
		return onView(withId(R.id.select_date));
	}

	public static ViewInteraction location() {
		return onView(withId(R.id.hotel_location_autocomplete));
	}

	public static ViewInteraction filterHotelName() {
		return onView(allOf(withId(R.id.filter_hotel_name_edit_text), hasSibling(withId(R.id.hotel_name))));
	}

	public static ViewInteraction doneButton() {
		return onView(allOf(withId(R.id.search_btn), isDescendantOfA(withId(R.id.filter_toolbar))));
	}

	public static ViewInteraction clearButton() {
		return onView(withId(R.id.clear_location_button));
	}

	public static ViewInteraction etpAndFreeCancellationMessagingContainer() {
		return onView(withId(R.id.etp_and_free_cancellation_messaging_container));
	}

	public static ViewInteraction etpInfoText() {
		return onView(withId(R.id.etp_info_text));
	}

	public static ViewInteraction freeCancellation() {
		return onView(withId(R.id.free_cancellation));
	}

	public static ViewInteraction horizontalDividerBwEtpAndFreeCancellation() {
		return onView(withId(R.id.horizontal_divider_bw_etp_and_free_cancellation));
	}

	public static ViewInteraction etpPlaceholder() {
		return onView(withId(R.id.etp_placeholder));
	}

	public static ViewInteraction payNowAndLaterOptions() {
		return onView(withId(R.id.radius_pay_options));
	}

	public static ViewInteraction roomsContainer() {
		return onView(withId(R.id.room_container));
	}

	public static ViewInteraction resortFeesText() {
		return onView(withId(R.id.resort_fees_text));
	}

	public static ViewInteraction ratingContainer() {
		return onView(withId(R.id.rating_container));
	}

	public static ViewInteraction amenityContainer() {
		return onView(withId(R.id.amenities_table_row));
	}

	public static ViewInteraction commonAmenitiesText() {
		return onView(withId(R.id.common_amenities_text));
	}

	public static ViewInteraction renovationContainer() {
		return onView(withId(R.id.renovation_container));
	}

	public static void selectLocation(String hotel) throws Throwable {
		Common.delay(1);
		hotelSuggestionList().perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(hotel)), click()));
	}

	public static ViewInteraction suggestionMatches(Matcher<View> matcher, int position) throws Throwable {
		return hotelSuggestionList()
			.check(RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(matcher)));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}

	public static ViewInteraction searchButton() {
		return onView(allOf(withId(R.id.search_btn), isDescendantOfA(hasSibling(withId(R.id.search_container)))));
	}

	public static void clickSearchButton() {
		searchButton().perform(click());
	}

	public static ViewInteraction hotelResultsList() {
		return onView(withId(R.id.list_view));
	}

	public static ViewInteraction hotelResultsMap() {
		return onView(allOf(withId(R.id.map_view), hasSibling(withId(R.id.hotel_carousel_container))));
	}

	public static ViewInteraction mapfab() {
		return onView(withId(R.id.fab));
	}


	public static ViewInteraction hotelSuggestionList() {
		return onView(withId(R.id.drop_down_list));
	}

	public static void selectHotel(int position) {
		hotelResultsList().perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
	}

	public static void selectHotelWithName(String name) {
		hotelResultsList().perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(name)), click()));
	}

	public static void clickSortFilter() {
		onView(withId(R.id.sort_filter_button_container)).perform(click());
	}

	public static ViewInteraction filterVip() {
		return onView(withId(R.id.filter_hotel_vip));
	}

	public static ViewInteraction clearFilter() {
		return onView(
			allOf(withId(R.id.dynamic_feedback_clear_button), isDescendantOfA(withId(R.id.dynamic_feedback_container))));
	}

	public static ViewInteraction filterResultsSnackBar() {
		return onView(withId(R.id.dynamic_feedback_container));
	}

	public static ViewInteraction filterResultsSnackBarCounter() {
		return onView(allOf(withId(R.id.dynamic_feedback_counter),
			isDescendantOfA(withId(R.id.dynamic_feedback_container))));
	}

	public static ViewInteraction addRoom() {
		return onView(
			allOf(
				withId(R.id.view_room_button), allOf(withText("Book")),
				isDescendantOfA(allOf(withId(R.id.collapsed_container))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static ViewInteraction viewRoom(String roomName) {
		return onView(
			allOf(
				withId(R.id.view_room_button), allOf(withText("View Room")),
				isDescendantOfA(allOf(withId(R.id.collapsed_container),
					withChild(allOf(withId(R.id.room_type_text_view), withText(roomName))))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static void clickAddRoom() {
		addRoom().perform(scrollTo(), click());
	}

	public static void clickViewRoom(String roomName) {
		viewRoom(roomName).perform(scrollTo(), click());
	}

	public static void clickPayLater() {
		onView(withId(R.id.radius_pay_later)).perform(click());
	}

	public static void clickPayNow() {
		onView(withId(R.id.radius_pay_now)).perform(click());
	}

	public static void clickRatingContainer() {
		onView(withId(R.id.rating_container)).perform(scrollTo(), click());
	}

	public static void waitForResultsDisplayed() {
		hotelResultsList().perform(ViewActions.waitForViewToDisplay());
	}

	public static void waitForMapDisplayed() {
		hotelResultsMap().perform(ViewActions.waitForViewToDisplay());
	}

	public static void waitForDetailsDisplayed() {
		onView(withId(R.id.rating_container)).perform(ViewActions.waitForViewToDisplay());
	}

	public static ViewInteraction resultsListItemView(Matcher<View> identifyingMatcher) {
		return ViewActions.recyclerItemView(identifyingMatcher, R.id.list_view);
	}

	public static void showCalendar() {
		calendar().check(matches((isDisplayed())));
	}

	public static ViewInteraction guestPicker() {
		return onView(withId(R.id.select_traveler));
	}

	public static ViewInteraction adultPicker() {
		return onView(withId(R.id.adult));
	}

	public static ViewInteraction childPicker() {
		return onView(withId(R.id.children));
	}

	public static ViewInteraction childAgeLabel() {
		return onView(withId(R.id.children_age_label));
	}

	public static void doSearch() throws Throwable {
		final LocalDate start = DateTime.now().toLocalDate();
		final LocalDate end = start.plusDays(3);

		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(start, end);

		HotelScreen.clickSearchButton();
		HotelScreen.waitForResultsDisplayed();
	}

	public static void selectHotel(String name) throws Throwable {
		HotelScreen.selectHotelWithName(name);
		HotelScreen.waitForDetailsDisplayed();
	}

	public static void selectRoom() throws Throwable {
		HotelScreen.clickAddRoom();
	}

	public static void pickRoom(String name) {
		HotelScreen.clickViewRoom(name);
		HotelScreen.clickAddRoom();
	}

	public static void sortFilter() {
		HotelScreen.clickSortFilter();
	}

	public static void checkout(boolean walletSupported) throws Throwable {
		CheckoutViewModel.waitForCheckout();
		CheckoutViewModel.clickDone();
		CheckoutViewModel.enterTravelerInfo();
		if (walletSupported) {
			CheckoutViewModel.enterPaymentInfoHotels();
		}
		else {
			CheckoutViewModel.enterPaymentInfo();
		}
		CheckoutViewModel.pressClose();
	}

	public static void slideToPurchase() throws Throwable {
		CheckoutViewModel.performSlideToPurchase();
		CVVEntryScreen.waitForCvvScreen();
	}

	public static void enterCVV() throws Throwable {
		CVVEntryScreen.enterCVV("123");
		CVVEntryScreen.clickBookButton();
	}

	public static void verifyPriceChange(String price) throws Throwable {
		onView(withId(R.id.price_change_text)).perform(ViewActions.waitForViewToDisplay());
		assertViewWithTextIsDisplayed(R.id.price_change_text, price);
		assertViewIsDisplayed(R.id.price_change_container);
	}

	public static ViewInteraction selectRoomButton() throws Throwable {
		return onView(withId(R.id.select_room_button));
	}

	public static void clickVIPAccess() {
		onView(withId(R.id.vip_access_message)).perform(click());
	}

	public static void clickDetailsMiniMap() {
		onView(withId(R.id.map_click_container)).perform(scrollTo(), click());
	}

	public static void clickSelectARoomInFullMap() {
		onView(withId(R.id.map_view_select_room_container)).perform(click());
	}
	

	public static void clickSignIn() {
		onView(withId(R.id.login_text_view)).perform(click());
	}
}
