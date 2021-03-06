package com.expedia.bookings.test.phone.cars;

import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.view.View;
import android.widget.ImageButton;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.TabletViewActions;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.setVisibility;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public final class CarScreen {

	// Search

	public static void didNotGoToResults() {
		EspressoUtils.assertViewIsDisplayed(R.id.search_container);
	}

	public static void showCalendar() {
		calendar().check(matches((isDisplayed())));
	}

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static ViewInteraction pickupLocation() {
		return onView(withId(R.id.pickup_location));
	}

	public static void waitForSearchScreen() {
		pickupLocation().perform(waitForViewToDisplay());
	}

	public static ViewInteraction searchFilter() {
		return onView(withId(R.id.sort_toolbar));
	}

	public static void clickFilterDone() {
		onView(allOf(withId(R.id.search_btn), withText(R.string.done))).perform(click());
	}

	public static void selectPickupLocation(String airportCode) throws Throwable {
		Common.delay(1);
		onView(withText(airportCode))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity(
				).getWindow().getDecorView()))))
			.perform(click());
	}

	public static void selectAirport(String airportCode, String displayName) throws Throwable {
		pickupLocation().perform(typeText(airportCode));
		selectPickupLocation(displayName);
	}

	public static ViewInteraction dropOffLocation() {
		return onView(withId(R.id.dropoff_location));
	}

	public static ViewInteraction selectDateButton() {
		return onView(withId(R.id.select_date));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}

	public static ViewInteraction dropOffTimeBar() {
		return onView(withId(R.id.dropoff_time_seek_bar));
	}

	public static ViewInteraction pickUpTimeBar() {
		return onView(withId(R.id.pickup_time_seek_bar));
	}

	public static ViewInteraction alertDialog() {
		return onView(withChild(withId(android.R.id.button1)));
	}

	public static ViewInteraction alertDialogMessage() {
		return onView(withId(android.R.id.message));
	}

	public static ViewInteraction alertDialogPositiveButton() {
		return onView(withId(android.R.id.button1));
	}

	public static ViewInteraction searchButton() {
		return onView(allOf(withId(R.id.search_btn), isDescendantOfA(hasSibling(withId(R.id.search_container)))));
	}

	// Results

	public static ViewInteraction carCategoryList() {
		return onView(withId(R.id.category_list));
	}

	public static void selectCarCategory(String name) {
		carCategoryList().perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(name)), click()));
	}

	public static ViewInteraction searchErrorWidgetButton() {
		return onView(allOf(withId(R.id.error_action_button), isDescendantOfA(withId(R.id.search_error_widget))));
	}

	public static ViewInteraction searchErrorToolbarBack() {
		return onView(allOf(isAssignableFrom(ImageButton.class), isDescendantOfA(allOf(withId(R.id.error_toolbar), isDescendantOfA(withId(R.id.car_results_presenter))))));
	}

	public static ViewInteraction searchWidgetToolbarBack() {
		return onView(allOf(isDescendantOfA(withId(R.id.widget_car_params)), withParent(withId(R.id.toolbar)),
			isAssignableFrom(ImageButton.class)));
	}

	//Filters

	public static void clickFilterButton() {
		searchFilter().perform(click());
	}

	public static void selectCategoryForFilter(String categoryName) {
		onView(allOf(withId(R.id.category_check_box), hasSibling(withText(categoryName)))).perform(click());
	}

	public static void hideFilterButton() {
		searchFilter().perform(setVisibility(View.GONE));
	}

	// Details

	public static ViewInteraction carOfferList() {
		return onView(withId(R.id.offer_list));
	}

	public static void expandCarOffer(int position) {
		hideFilterButton();
		//To make the selection of an item work correctly on smaller screen size phones
		//we need to scroll to the next item
		int listCount = EspressoUtils.getListCount(CarScreen.carOfferList());
		if (listCount >= position + 1) {
			CarScreen.carOfferList().perform(RecyclerViewActions.scrollToPosition(position + 1));
		}
		else {
			CarScreen.carOfferList().perform(RecyclerViewActions.scrollToPosition(position));
		}

		carOfferList().perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
	}

	public static void selectCategoryFilter(String categoryName) {
		onView(allOf(withId(R.id.category), withText(categoryName))).perform(click());

	}

	public static void selectSupplierFilter(String supplierName) {
		onView(allOf(withId(R.id.vendor), withText(supplierName),
			isDescendantOfA(withId(R.id.filter_suppliers)))).perform(scrollTo(), click());

	}

	public static void selectCarOffer(int carOfferNum) throws Throwable {
		//Selecting an already expanded offer opens google maps
		if (carOfferNum != 0) {
			CarScreen.expandCarOffer(carOfferNum);
		}
		onView(allOf(isDescendantOfA(withId(R.id.offer_list)), withId(R.id.reserve_now), withText("Reserve")))
			.perform(click());
		Common.delay(1);
	}

	// Checkout

	public static ViewInteraction travelerWidget() {
		return onView(withId(R.id.main_contact_info_card_view));
	}

	public static ViewInteraction firstName() {
		return onView(withId(R.id.edit_first_name));
	}

	public static ViewInteraction lastName() {
		return onView(withId(R.id.edit_last_name));
	}

	public static ViewInteraction email() {
		return onView(withId(R.id.edit_email_address));
	}

	public static ViewInteraction phoneNumber() {
		return onView(withId(R.id.edit_phone_number));
	}

	public static ViewInteraction checkoutToolbarDone() {
		return onView(withId(R.id.menu_done));
	}

	public static ViewInteraction checkoutTotalPrice() {
		return onView(withId(R.id.price_text));
	}

	public static ViewInteraction checkoutErrorScreen() {
		return onView(withId(R.id.checkout_error_widget));
	}

	public static ViewInteraction checkoutErrorText() {
		return onView(allOf(isDescendantOfA(withId(R.id.checkout_error_widget)), withId(R.id.error_text)));
	}

	public static ViewInteraction checkoutErrorButton() {
		return onView(allOf(isDescendantOfA(withId(R.id.checkout_error_widget)), withId(R.id.error_action_button)));
	}

}
