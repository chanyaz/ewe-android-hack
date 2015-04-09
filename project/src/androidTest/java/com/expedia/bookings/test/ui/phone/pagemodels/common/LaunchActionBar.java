package com.expedia.bookings.test.ui.phone.pagemodels.common;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressMenuKey;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


/**
 * Created by dmadan on 4/7/14.
 */
public class LaunchActionBar {
	private static final int sSettingsStringID = R.string.Settings;
	private static final int sInfoStringID = R.string.Info;
	private static final int sTripsStringID = R.string.Your_Trips;
	private static final int sShopStringID = R.string.shop;
	private static final int sLogOutStringID = R.string.sign_out;
	private static final int sAddItinButtonID = R.id.add_itinerary;

	public static ViewInteraction settingsString() {
		return onView(withText(sSettingsStringID));
	}

	public static ViewInteraction infoString() {
		return onView(withText(sInfoStringID));
	}

	public static ViewInteraction logOutString() {
		return onView(withText(sLogOutStringID));
	}

	public static ViewInteraction tripsString() {
		return onView(withText(sTripsStringID));
	}

	public static ViewInteraction shopString() {
		return onView(withText(sShopStringID));
	}

	public static ViewInteraction addItinButton() {
		return onView(withId(sAddItinButtonID));
	}
	/*
	 * ActionBar tabs
	 * They don't have IDs, so we must reference by string ID
	 */

	public static void pressShop() {
		shopString().perform(click());
	}

	public static void pressTrips() {
		tripsString().perform(click());
	}

	/*
	 * Menu interactions
	 */

	public static void openMenuDropDown() {
		onView(isRoot()).perform(pressMenuKey());
	}

	public static void pressSettings() {
		settingsString().perform(click());
	}

	public static void pressInfo() {
		infoString().perform(click());
	}

	public static void pressLogOut() {
		logOutString().perform(click());
	}

	/*
	 * Other actions
	 */

	public static void pressAddItinButton() {
		addItinButton().perform(click());
	}

	public static void clickActionBarHomeIcon() {
		onView(withId(android.R.id.home)).perform(click());
	}

}
