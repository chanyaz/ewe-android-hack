package com.expedia.bookings.test.phone.hotels;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;

public class HotelScreen {

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static ViewInteraction reviews() {
		return onView(withId(R.id.widget_hotel_reviews));
	}

	public static ViewInteraction filterStarRatingOne() {
		return onView(allOf(withId(R.id.hotel_filter_rating_one), isDescendantOfA(withId(R.id.hotel_filter_view))));
	}

	public static ViewInteraction filterStarRatingFour() {
		return onView(allOf(withId(R.id.hotel_filter_rating_four), isDescendantOfA(withId(R.id.hotel_filter_view))));
	}

	public static ViewInteraction filterHotelName() {
		return onView(allOf(withId(R.id.filter_hotel_name_edit_text), isDescendantOfA(withId(R.id.hotel_filter_view))));
	}

	public static ViewInteraction doneButton() {
		return onView(allOf(withId(R.id.search_btn), isDescendantOfA(withId(R.id.filter_toolbar))));
	}

	public static ViewInteraction etpAndFreeCancellationMessagingContainer() {
		return onView(withId(R.id.etp_and_free_cancellation_messaging_container));
	}

	public static ViewInteraction etpInfoText() {
		return onView(withId(R.id.etp_info_text));
	}

	public static ViewInteraction etpInfoTextSmall() {
		return onView(withId(R.id.etp_info_text_small));
	}

	public static ViewInteraction freeCancellation() {
		return onView(withId(R.id.free_cancellation));
	}

	public static ViewInteraction freeCancellationSmall() {
		return onView(withId(R.id.free_cancellation_small));
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

	public static ViewInteraction expandedFreeCancellation() {
		return onView(
			allOf(withId(R.id.expanded_free_cancellation_text_view),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static ViewInteraction expandedBedType() {
		return onView(
			allOf(withId(R.id.expanded_bed_type_text_view), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static void clickRenoInfo() {
		onView(withId(R.id.renovation_container)).perform(scrollTo(), click());
	}

	public static ViewInteraction hotelResultsList() {
		return onView(withId(R.id.list_view));
	}

	public static ViewInteraction launchList() {
		return onView(withId(R.id.launch_list_widget));
	}

	public static ViewInteraction hotelResultsMap() {
		return onView(allOf(withId(R.id.map_view)));
	}

	public static ViewInteraction mapFab() {
		return onView(withId(R.id.fab));
	}

	public static ViewInteraction hotelDetailsStarRating() {
		return onView(withId(R.id.hotel_star_rating_bar));
	}

	public static ViewInteraction hotelResultsToolbar() {
		return onView(withId(R.id.hotel_results_toolbar));
	}

	public static ViewInteraction hotelCarousel() {
		return onView(withId(R.id.hotel_carousel));
	}

	public static void clickSortFilter() {
		onView(withId(R.id.sort_filter_button_container)).perform(click());
	}

	public static ViewInteraction propertyInfoContainer() {
		return onView(withId(R.id.property_info_container));
	}

	public static ViewInteraction filterVip() {
		return onView(allOf(withId(R.id.filter_hotel_vip), isDescendantOfA(withId(R.id.hotel_filter_view))));
	}

	public static ViewInteraction filterVipView() {
		return onView(allOf(withId(R.id.filter_vip_view), isDescendantOfA(withId(R.id.hotel_filter_view))));
	}

	public static ViewInteraction clearFilter() {
		return onView(
			allOf(withId(R.id.dynamic_feedback_clear_button),
				isDescendantOfA(withId(R.id.dynamic_feedback_container))));
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
			allOf(withId(R.id.hotel_book_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static ViewInteraction viewRoom(String roomName) {
		return onView(
			allOf(
				withId(R.id.view_room_button),
				withParent(allOf(
					withId(R.id.hotel_room_row_button),
					withParent(allOf(
						withId(R.id.earn_row_button_container),
						hasSibling(allOf(
							withId(R.id.parent_room_type_and_price_container),
							withChild(allOf(
								withId(R.id.room_type_text_view), withText(roomName))))))))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static void clickAddRoom() {
		waitForDetailsLoaded();
		addRoom().perform(scrollTo(), click());
	}

	public static void clickViewRoom(String roomName) {
		waitForDetailsLoaded();
		viewRoom(roomName).perform(scrollTo(), click());
	}

	public static void scrollToPropertyInfoContainer() {
		propertyInfoContainer().perform(scrollTo());
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

	public static void waitForResultsLoaded() {
		waitForResultsLoaded(10);
	}

	public static void waitForResultsLoaded(int seconds) {
		Matcher<View> resultListMatcher = hasDescendant(withId(R.id.list_view));
		onView(anyOf(withId(R.id.hotel_presenter), withId(R.id.package_hotel_presenter)))
			.perform(ViewActions.waitFor(resultListMatcher, 10, TimeUnit.SECONDS));

		hotelResultsList().perform(waitForViewToDisplay());

		Matcher<View> pshMatcher = hasDescendant(
			allOf(withId(R.id.results_description_header), not(withText(R.string.progress_searching_hotels_hundreds)),
				isDisplayed()));
		hotelResultsList().perform(ViewActions.waitFor(pshMatcher, seconds, TimeUnit.SECONDS));
	}

	public static void waitForMapDisplayed() {
		hotelResultsMap().perform(waitForViewToDisplay());
	}

	public static void waitForDetailsLoaded() {
		onView(withId(R.id.hotel_detail)).perform(waitForViewToDisplay());
	}

	public static void waitForErrorDisplayed() {
		onView(withId(R.id.widget_hotel_errors)).perform(waitForViewToDisplay());
	}

	public static void waitForConfirmationDisplayed() {
		onView(withId(R.id.hotel_confirmation_presenter)).perform(waitForViewToDisplay());
	}

	public static void waitForFilterDisplayed() {
		onView(withId(R.id.hotel_filter_view)).perform(waitForViewToDisplay());
	}

	public static void assertCalendarShown() {
		calendar().check(matches((isDisplayed())));
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

	public static void enterGenericSearchParams() throws Throwable {
		SearchScreen.origin().perform(click());
		SearchScreen.selectDestination();

		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
	}

	public static void clickSwPToggle() throws Throwable {
		onView(withId(R.id.swp_switch)).perform(click());
	}

	public static void selectHotel(String name) throws Throwable {
		waitForResultsLoaded();
		hotelResultsList().perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(name)), click()));
		HotelScreen.waitForDetailsLoaded();
	}

	public static void selectHotel() throws Throwable {
		selectHotel("happypath");
	}

	public static void selectRoom() throws Throwable {
		HotelScreen.clickAddRoom();
		Common.delay(2);
	}

	public static void pickRoom(String name) {
		HotelScreen.clickViewRoom(name);
		HotelScreen.clickAddRoom();
	}

	public static void sortFilter() {
		waitForResultsLoaded();
		clickSortFilter();
		waitForFilterDisplayed();
	}

	public static void doLogin() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.login_widget);
		CheckoutViewModel.enterLoginDetails();
		CheckoutViewModel.pressDoLogin();
		Common.delay(1);
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
	}

	public static void checkoutAfterSignIn(boolean walletSupported) throws Throwable {
		if (walletSupported) {
			CheckoutViewModel.enterPaymentInfoHotels();
		}
		else {
			CheckoutViewModel.enterPaymentInfo();
		}
		CheckoutViewModel.pressClose();
	}

	public static void checkoutWithPointsOnly() {
		CheckoutViewModel.enterPaymentInfo(true);
	}

	public static void checkoutWithPointsAndCard() {
		CheckoutViewModel.enterPaymentInfo(false);
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


	public static ViewInteraction selectRoomButton() throws Throwable {
		return onView(withId(R.id.select_room_button));
	}

	public static void clickSelectRoom() throws Throwable {
		selectRoomButton().perform(waitForViewToDisplay(), click());
	}

	public static void clickVIPAccess() {
		onView(withId(R.id.vip_access_message_container)).perform(click());
	}

	public static void clickDetailsMiniMap() {
		onView(withId(R.id.map_click_container)).perform(scrollTo(), click());
	}

	public static void clickSelectARoomInFullMap() {
		onView(withId(R.id.map_view_select_room_container)).perform(click());
	}

	public static void clickSignIn() {
		onView(withId(R.id.login_text_view)).perform(scrollTo(), click());
	}

	public static void clickSignOut() {
		onView(withId(R.id.account_logout_logout_button)).perform(waitForViewToDisplay());
		onView(withId(R.id.account_logout_logout_button)).perform(click());
		onView(
			allOf(withId(android.R.id.message), withText("Are you sure you want to sign out of your Expedia account?")))
			.check(matches(isDisplayed()));
		onView(withId(android.R.id.button1)).perform(click());
	}

	public static void signIn() {
		signIn("qa-ehcc@mobiata.com");
	}

	public static void signIn(String username) {
		LogInScreen.typeTextEmailEditText(username);
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();
	}

	public static void enterCVVAndBook() throws Throwable {
		CVVEntryScreen.enterCVV("123");
		CVVEntryScreen.clickBookButton();
	}

	public static void clickRoom(String room) {
		onView(withText(room)).perform(scrollTo(), click());
	}

}
