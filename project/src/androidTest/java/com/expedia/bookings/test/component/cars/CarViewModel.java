package com.expedia.bookings.test.component.cars;

import org.joda.time.LocalDate;

import android.app.Instrumentation;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.espresso.TabletViewActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.SpoonScreenshotUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public final class CarViewModel {

	// Search

	public static void didNotGoToResults() {
		EspressoUtils.assertViewIsDisplayed(R.id.search_container);
	}

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static ViewInteraction pickupLocation() {
		return onView(withId(R.id.pickup_location));
	}

	public static ViewInteraction searchFilter() {
		return onView(withId(R.id.sort_toolbar));
	}

	public static void clickFilterDone() {
		onView(withId(R.id.car_filter_done)).perform(click());
	}
	public static void selectPickupLocation(Instrumentation instrumentation, String airportCode) throws Throwable {
		ScreenActions.delay(1);
		onView(withText(airportCode))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity(instrumentation).getWindow().getDecorView()))))
			.perform(click());
	}

	public static void selectAirport(Instrumentation instrumentation, String airportCode,
		String displayName) throws Throwable {
		pickupLocation().perform(typeText(airportCode));
		selectPickupLocation(instrumentation, displayName);
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

	public static ViewInteraction alertDialogNeutralButton() {
		return onView(withId(android.R.id.button3));
	}

	public static ViewInteraction searchButton() {
		return onView(withId(R.id.menu_check));
	}

	// Results

	public static ViewInteraction carCategoryList() {
		return onView(withId(R.id.category_list));
	}

	public static void selectCarCategory(int position) {
		carCategoryList().perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
	}

	public static void selectCarCategory(String name) {
		carCategoryList().perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(name)), click()));
	}

	// Details

	public static ViewInteraction carOfferList() {
		return onView(withId(R.id.offer_list));
	}

	public static void expandCarOffer(int position) {
		carOfferList().perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
	}

	public static void selectCarOffer(int carOfferNum) throws Throwable {
		//Selecting an already expanded offer opens google maps
		if (carOfferNum != 0) {
			CarViewModel.expandCarOffer(carOfferNum);
		}
		onView(allOf(isDescendantOfA(withId(R.id.offer_list)), withId(R.id.reserve_now), withText("Reserve")))
			.perform(click());
		ScreenActions.delay(1);
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

	public static ViewInteraction checkoutToolbarNext() {
		return onView(withId(R.id.menu_next));
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

	// Confirmation
	public static ViewInteraction confirmationNumber() {
		return onView(withId(R.id.confirmation_text));
	}

}
