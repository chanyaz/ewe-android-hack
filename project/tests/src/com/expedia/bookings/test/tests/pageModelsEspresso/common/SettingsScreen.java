package com.expedia.bookings.test.tests.pageModelsEspresso.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import org.hamcrest.Matchers;

/**
 * Created by dmadan on 4/7/14.
 */
public class SettingsScreen extends ScreenActions {
	private static final String TAG = "SettingsScreen POM";
	private static final int SELECT_POS_STRING_ID = R.string.preference_point_of_sale_title;
	private static final int CLEAR_PRIVATE_DATE_STRING_ID = R.string.clear_private_data;
	private static final int OK_STRING_ID = R.string.ok;
	private static final int ACCEPT_STRING_ID = R.string.accept;
	private static final int SUPPRESS_HOTEL_BOOKING_CHECKBOX_ID = R.id.preference_suppress_hotel_booking_checkbox;
	private static final int SUPPRESS_FLIGHT_BOOKING_CHECKBOX_ID = R.id.preference_suppress_flight_booking_checkbox;
	private static final int CANCEL_STRING_ID = R.string.cancel;

	private static final int COUNTRY_STRING_ID = R.string.preference_point_of_sale_title;
	private static final String SELECT_API_STRING_ID = "Select API";
	private static final String SERVER_PROXY_STRING_ID = "Server/Proxy Address";
	private static final String STUB_CONFIGURATION_PAGE_STRING_ID = "Stub Configuration Page";
	private static final String SUPPRESS_HOTELS_BOOKING_STRING_ID = "Suppress Hotel Bookings";
	private static final String SUPPRESS_FLIGHTS_BOOKING_STRING_ID = "Suppress Flight Bookings";

	// Object access
	public static ViewInteraction clearPrivateDataString() {
		return onView(withText(CLEAR_PRIVATE_DATE_STRING_ID));
	}

	public static ViewInteraction OKString() {
		return onView(withText(OK_STRING_ID));
	}

	public static ViewInteraction cancelString() {
		return onView(withText(CANCEL_STRING_ID));
	}

	public static ViewInteraction AcceptString() {
		return onView(withText(ACCEPT_STRING_ID));
	}

	public static ViewInteraction country() {
		return onView(withText(COUNTRY_STRING_ID));
	}

	// Object interaction

	public static void clickCountryString() {
		country().perform(click());
	}

	public static void clickToClearPrivateData() {
		clearPrivateDataString().perform(click());
	}

	public static void clickOKString() {
		OKString().perform(click());
	}

	public static void clickCancelString() {
		cancelString().perform(click());
	}

	public static void clickAcceptString() {
		AcceptString().perform(click());
	}

	public static void clickSelectAPIString() {
		onView(withText(SELECT_API_STRING_ID)).perform(click());
	}

	public static void clickServerProxyAddressString() {
		onView(withText(SERVER_PROXY_STRING_ID)).perform(click());
	}

	public static void clickStubConfigPage() {
		onView(withText(STUB_CONFIGURATION_PAGE_STRING_ID)).perform(click());
	}

	public static void clickMobileFlightCheckoutScenario() {
		onData(allOf(is(String.class), Matchers.equalTo("MobileFlightCheckoutUK"))).inAdapterView(withId(android.R.id.list)).perform(click());
	}

	public static void clickHotelCheckoutScenario() {
		onData(allOf(is(String.class), Matchers.equalTo("MobileHotelCheckoutUK"))).inAdapterView(withId(android.R.id.list)).perform(click());
	}
}
