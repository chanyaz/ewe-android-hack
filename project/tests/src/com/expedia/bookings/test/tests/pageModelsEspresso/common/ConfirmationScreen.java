package com.expedia.bookings.test.tests.pageModelsEspresso.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
public class ConfirmationScreen extends ScreenActions {
	private static final int DONE_BUTTON_ID = R.id.menu_done;
	private static final int ITIN_TEXT_VIEW_ID = R.id.itinerary_text_view;
	private static final int EMAIL_TEXT_VIEW_ID = R.id.email_text_view;
	private static final int CALL_EXPEDIA_TEXT_VIEW_ID = R.id.call_action_text_view;
	private static final int CALL_CUSTOMER_SUPPORT_STRING = R.string.call_expedia_customer_support;
	private static final int SHARE_VIA_EMAIL_TEXT_VIEW = R.id.share_action_text_view;
	private static final int ADD_TO_CALENDAR_TEXT_VIEW_ID = R.id.calendar_action_text_view;
	private static final int BOOKING_COMPLETE_STRING_ID = R.string.booking_complete;

	// Object access

	public static ViewInteraction doneButton() {
		return onView(withId(DONE_BUTTON_ID));
	}

	public static ViewInteraction itineraryTextView() {
		return onView(withId(ITIN_TEXT_VIEW_ID));
	}

	public static ViewInteraction emailTextView() {
		return onView(withId(EMAIL_TEXT_VIEW_ID));
	}

	public static ViewInteraction callExpediaTextView() {
		return onView(withId(CALL_EXPEDIA_TEXT_VIEW_ID));
	}

	public static ViewInteraction callExpediaCustomerSupport() {
		return onView(withText(CALL_CUSTOMER_SUPPORT_STRING));
	}

	public static ViewInteraction shareViaEmailTextView() {
		return onView(withId(SHARE_VIA_EMAIL_TEXT_VIEW));
	}

	public static ViewInteraction addToCalendarTextView() {
		return onView(withId(ADD_TO_CALENDAR_TEXT_VIEW_ID));
	}

	public static ViewInteraction bookingComplete() {
		return onView(withText(BOOKING_COMPLETE_STRING_ID));
	}

	// Object interaction

	public static void clickDoneButton() {
		doneButton().perform(click());
	}

	public static void clickCallExpedia() {
		callExpediaTextView().perform(click());
	}

	public static void clickShareViaEmail() {
		shareViaEmailTextView().perform(click());
	}

	public static void clickAddToCalendar() {
		addToCalendarTextView().perform(click());
	}
}
