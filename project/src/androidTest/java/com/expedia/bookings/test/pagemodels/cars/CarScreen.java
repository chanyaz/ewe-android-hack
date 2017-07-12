package com.expedia.bookings.test.pagemodels.cars;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
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

	public static ViewInteraction calendarCard() {
		return onView(withId(R.id.car_calendar_card));
	}

	public static ViewInteraction locationCardView() {
		return onView(withId(R.id.origin_card));
	}

	public static ViewInteraction pickupLocation() {
		return onView(withId(R.id.search_src_text));
	}


	public static void waitForSearchScreen() {
		pickupLocation().perform(waitForViewToDisplay());
	}

	public static ViewInteraction searchFilter() {
		return onView(withId(R.id.sort_toolbar));
	}

	public static ViewInteraction searchCategoryFilter() {
		return onView(withId(R.id.category_sort_toolbar));
	}

	public static void clickFilterDone() {
		onView(allOf(withId(R.id.search_btn), withText(R.string.done))).perform(click());
	}

	public static void selectPickupLocation(String airportCode) throws Throwable {
		Common.delay(1);
		SearchScreen.selectLocation(airportCode);
	}

	public static void selectAirport(String airportCode, String displayName) throws Throwable {
		pickupLocation().perform(ViewActions.waitForViewToDisplay(), typeText(airportCode));
		selectPickupLocation(displayName);
	}

	public static ViewInteraction dropOffLocation() {
		return onView(withId(R.id.destination_card));
	}

	public static ViewInteraction selectDateButton() {
		return onView(allOf(withId(R.id.dateLabel), withParent(withId(R.id.car_calendar_card))));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		SearchScreen.selectDates(start, end);
	}

	public static ViewInteraction dropOffTimeBar(Activity activity) {
		return onView(withId(R.id.return_time_slider)).inRoot(withDecorView(not(is((activity.getWindow().getDecorView())))));
	}

	public static ViewInteraction pickUpTimeBar(Activity activity) {
		return onView(withId(R.id.depart_time_slider)).inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))));
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
		return SearchScreen.searchButton();
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
		return onView(allOf(isAssignableFrom(AppCompatImageButton.class), isDescendantOfA(
			allOf(withId(R.id.error_toolbar), isDescendantOfA(withId(R.id.car_results_presenter))))));
	}

	public static ViewInteraction searchWidgetToolbarBack() {
		return onView(allOf(isDescendantOfA(withId(R.id.widget_car_params)), withParent(withId(R.id.toolbar)),
			isAssignableFrom(AppCompatImageButton.class)));
	}

	//Filters

	public static void clickFilterButton() {
		searchFilter().perform(click());
	}

	public static void clickCategoryFilterButton() {
		searchCategoryFilter().perform(click());
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
		Common.delay(1);
		onView(allOf(isDescendantOfA(withId(R.id.offer_list)), withId(R.id.reserve_now), withText("Reserve")))
			.perform(click());
		Common.delay(1);
	}

	public static ViewInteraction searchButtonOnDetails() {
		return onView(allOf(withId(R.id.menu_search), isDescendantOfA((withId(R.id.car_results_presenter)))));
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
