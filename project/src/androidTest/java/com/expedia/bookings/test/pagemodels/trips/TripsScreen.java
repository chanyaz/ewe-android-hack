package com.expedia.bookings.test.pagemodels.trips;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

public class TripsScreen {
	private static final int LOG_IN_BUTTON_ID = R.id.account_sign_in_container;
	private static final int LOG_IN_TEXT_VIEW_ID = R.id.login_text_view;
	private static final Matcher<View> tripsScreen = withId(R.id.card_layout);
	private static final Matcher<View> tripsItemName = withId(R.id.header_text_view);
	public static ViewInteraction addGuestItinButton() {
		return onView(withId(R.id.add_guest_itin_text_view));
	}

	public static ViewInteraction logInButton() {
		return onView(withId(LOG_IN_BUTTON_ID));
	}

	public static ViewInteraction refreshTripsButtonText() {
		return onView(allOf(withId(LOG_IN_TEXT_VIEW_ID), withText("Refresh your trips")));
	}

	public static void clickOnLogInButton() {
		logInButton().perform(click());
	}

	public static DataInteraction tripsListItem() {
		return onData(anything()).inAdapterView(withId(android.R.id.list));
	}

	public static ViewInteraction enterItinToolbarText() {
		return onView(allOf(withText("Find guest booked trip"), isDescendantOfA(withId(R.id.toolbar))));
	}

	public static void waitForTripsViewToLoad() {
		EspressoUtils.waitForViewNotYetInLayoutToDisplay(tripsScreen, 10, TimeUnit.SECONDS);
	}

	public static void verifyTripItemWithNameIsPresent(String itemName) {
		onView(tripsItemName).check(matches(withText(itemName)));
		onView(ViewMatchers.withId(R.id.viewpager)).check(matches(isDisplayed()));
	}

	public static void clickOnTripItemWithName(@NotNull String hotelName) {
		onView(tripsItemName).perform(click());
	}
}
