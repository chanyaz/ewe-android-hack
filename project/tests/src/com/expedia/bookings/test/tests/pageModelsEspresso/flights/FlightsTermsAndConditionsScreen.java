package com.expedia.bookings.test.tests.pageModelsEspresso.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/8/14.
 */
public class FlightsTermsAndConditionsScreen extends ScreenActions {
	private static final int TERMS_AND_CONDITIONS_STRING_ID = R.string.terms_and_conditions;
	private static final int PRIVACY_POLICY_STRING_ID = R.string.privacy_policy;
	private static final int BEST_PRICE_GUARANTEE_STRING_ID = R.string.best_price_guarantee;
	private static final int CANCELLATION_POLICY_STRING_ID = R.string.cancellation_policy;
	private static final int CANCELLATION_POLICY_TEXT_VIEW_ID = R.id.cancellation_policy_text_view;
	private static final int TERMS_OF_BOOKING_TEXT_VIEW_ID = R.id.terms_of_booking;

	// Object access

	public static ViewInteraction termsAndConditions() {
		return onView(withText(TERMS_AND_CONDITIONS_STRING_ID));
	}

	public static ViewInteraction privacyPolicy() {
		return onView(withText(PRIVACY_POLICY_STRING_ID));
	}

	public static ViewInteraction bestPriceGuarantee() {
		return onView(withText(BEST_PRICE_GUARANTEE_STRING_ID));
	}

	public static ViewInteraction cancellationPolicy() {
		return onView(withText(CANCELLATION_POLICY_STRING_ID));
	}

	public static ViewInteraction cancellationPolicyTextView() {
		return onView(withId(CANCELLATION_POLICY_TEXT_VIEW_ID));
	}

	public static ViewInteraction termsOfBookingTextView() {
		return onView(withId(TERMS_OF_BOOKING_TEXT_VIEW_ID));
	}

}
