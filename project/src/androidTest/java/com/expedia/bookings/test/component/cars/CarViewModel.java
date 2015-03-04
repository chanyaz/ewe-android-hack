package com.expedia.bookings.test.component.cars;

import org.joda.time.LocalDate;

import android.app.Instrumentation;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.espresso.TabletViewActions;
import com.expedia.bookings.test.ui.espresso.ViewActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.SpoonScreenshotUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
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
		ScreenActions.delay(1);
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
		return onView(withId(R.id.menu_check));
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

	public static void clickDriverInfo() {
		onView(withId(R.id.driver_info_card_view)).perform(click());
	}

	// Details

	public static ViewInteraction carOfferList() {
		return onView(withId(R.id.offer_list));
	}

	public static void selectCarOffer(int position) {
		onView(allOf(isDescendantOfA(withId(R.id.offer_list)), withId(R.id.reserve_now), withText("Reserve"))).perform(
			click());
	}

	// Checkout

	public static ViewInteraction firstName() {
		return onView(withId(R.id.edit_first_name));
	}

	public static void enterFirstName(String name) {
		firstName().perform(scrollTo(), typeText(name));
	}

	public static ViewInteraction lastName() {
		return onView(withId(R.id.edit_last_name));
	}

	public static void enterLastName(String name) {
		lastName().perform(scrollTo(), typeText(name));
	}

	public static ViewInteraction email() {
		return onView(withId(R.id.edit_email_address));
	}

	public static void enterEmail(String email) {
		email().perform(scrollTo(), typeText(email));
	}

	public static ViewInteraction phone() {
		return onView(withId(R.id.edit_phone_number));
	}

	public static void enterPhoneNumber(String number) {
		phone().perform(scrollTo(), typeText(number));
	}

	public static ViewInteraction paymentContainer() {
		return onView(withId(R.id.payment_info));
	}

	public static ViewInteraction checkoutDataEnterDone() {
		return onView(withId(R.id.menu_checkout));
	}

	public static void pressClose() {
		onView(withId(R.id.checkout_toolbar)).perform(ViewActions.getChildViewButton(0));
	}

	public static void pressDone() {
		checkoutDataEnterDone().perform(click());
	}

	public static ViewInteraction performSlideToPurchase() {
		return onView(withId(R.id.slide_to_purchase_widget)).perform(ViewActions.swipeRight());
	}

	// Confirmation

	public static ViewInteraction confirmationNumber() {
		return onView(withId(R.id.confirmation_text));
	}

}
