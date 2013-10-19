package com.expedia.bookings.test.tests.pageModels.flights;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

public class FlightsTermsAndConditionsScreen extends ScreenActions {

	private static final int TERMS_AND_CONDITIONS_STRING_ID = R.string.terms_and_conditions;
	private static final int PRIVACY_POLICY_STRING_ID = R.string.privacy_policy;
	private static final int BEST_PRICE_GUARANTEE_STRING_ID = R.string.best_price_guarantee;
	private static final int CANCELLATION_POLICY_STRING_ID = R.string.cancellation_policy;
	private static final int CANCELLATION_POLICY_TEXT_VIEW_ID = R.id.cancellation_policy_text_view;
	private static final int TERMS_OF_BOOKING_TEXT_VIEW_ID = R.id.terms_of_booking;

	public FlightsTermsAndConditionsScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public String termsAndConditions() {
		return getString(TERMS_AND_CONDITIONS_STRING_ID);
	}

	public String privacyPolicy() {
		return getString(PRIVACY_POLICY_STRING_ID);
	}

	public String bestPriceGuarantee() {
		return getString(BEST_PRICE_GUARANTEE_STRING_ID);
	}

	public String cancellationPolicy() {
		return getString(CANCELLATION_POLICY_STRING_ID);
	}

	public TextView cancellationPolicyTextView() {
		return (TextView) getView(CANCELLATION_POLICY_TEXT_VIEW_ID);
	}

	public TextView termsOfBookingTextView() {
		return (TextView) getView(TERMS_OF_BOOKING_TEXT_VIEW_ID);
	}

}
