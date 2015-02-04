package com.expedia.bookings.test.component.cars;

import org.joda.time.LocalDate;

import android.app.Instrumentation;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.espresso.TabletViewActions;
import com.expedia.bookings.test.ui.utils.SpoonScreenshotUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public final class CarViewModel {

	// Search

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static ViewInteraction pickupLocation() {
		return onView(withId(R.id.pickup_location));
	}

	public static void selectPickupLocation(Instrumentation instrumentation, String airportCode) throws Throwable {
		CarViewModel.pickupLocation().perform(typeText(airportCode));
		onView(withText(airportCode))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity(instrumentation).getWindow().getDecorView()))))
			.perform(click());
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
		return onView(withChild(withId(android.R.id.button3)));
	}

	public static ViewInteraction alertDialogMessage() {
		return onView(withId(android.R.id.message));
	}

	public static ViewInteraction alertDialogNeutralButton() {
		return onView(withId(android.R.id.button3));
	}

	public static ViewInteraction searchButton() {
		return onView(withId(R.id.search_btn));
	}

	// Results

	public static ViewInteraction resultsLoadingView() {
		return onView(withId(R.id.loading));
	}

	public static ViewInteraction carCategoryList() {
		return onView(withId(R.id.category_list));
	}

	public static void selectCarCategory(int position) {
		carCategoryList().perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
	}

	// Details

	public static ViewInteraction carOfferList() {
		return onView(withId(R.id.offer_list));
	}

	public static void selectCarOffer(int position) {
		carOfferList().perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
	}

	// Checkout

	public static ViewInteraction slideToCheckout() {
		return onView(withId(R.id.slide_to_purchase_widget));
	}

}
