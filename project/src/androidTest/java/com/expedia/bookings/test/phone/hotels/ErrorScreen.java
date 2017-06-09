package com.expedia.bookings.test.phone.hotels;

import android.support.test.espresso.ViewInteraction;
import android.support.v7.widget.AppCompatImageButton;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;

public class ErrorScreen {

	public static void clickOnEditPayment() {
		onView(withText(R.string.edit_payment)).perform(click());
	}

	public static void clickOnEditTravellerInfo() {
		onView(withText(R.string.edit_guest_details)).perform(click());
	}

	public static void clickOnEditSearch() {
		onView(withText(R.string.edit_search)).perform(click());
	}

	public static void clickOnRetry() {
		onView(withText(R.string.retry)).perform(click());
	}

	public static void clickOnSearchAgain() {
		onView(withText(R.string.search_again)).perform(click());
	}

	public static void clickOnItinerary() {
		onView(withText(R.string.my_trips)).perform(click());
	}

	public static void clickToolbarBack() {
		toolbarNavigationBack(R.id.error_toolbar).perform(click());
	}

	private static ViewInteraction toolbarNavigationBack(int id) {
		return onView(allOf(withParent(withId(id)), withClassName(is(AppCompatImageButton.class.getName()))));
	}

}
