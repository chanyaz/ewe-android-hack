package com.expedia.bookings.utils;

import java.io.File;

import org.json.JSONObject;

import android.content.Context;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Policy;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;

public class ConfirmationUtils {

	private static final String CONFIRMATION_DATA_FILE = "confirmation.dat";
	private static final String CONFIRMATION_DATA_VERSION_FILE = "confirmation-version.dat";

	//////////////////////////////////////////////////////////////////////////////////////////
	// Breadcrumb (reloading activity)

	public static boolean saveConfirmationData(Context context, SearchParams searchParams, Property property,
			Rate rate, BillingInfo billingInfo, BookingResponse bookingResponse, Rate discountRate) {
		Log.i("Saving confirmation data...");
		try {
			JSONObject data = new JSONObject();
			data.put(Codes.SEARCH_PARAMS, searchParams.toJson());
			data.put(Codes.PROPERTY, property.toJson());
			data.put(Codes.RATE, rate.toJson());
			data.put(Codes.BILLING_INFO, billingInfo.toJson());
			data.put(Codes.BOOKING_RESPONSE, bookingResponse.toJson());
			if (discountRate != null) {
				data.put(Codes.DISCOUNT_RATE, discountRate.toJson());
			}

			IoUtils.writeStringToFile(CONFIRMATION_DATA_VERSION_FILE,
					Integer.toString(AndroidUtils.getAppCode(context)), context);
			IoUtils.writeStringToFile(CONFIRMATION_DATA_FILE, data.toString(0), context);

			return true;
		}
		catch (Exception e) {
			Log.e("Could not save hotel confirmation data state.", e);
			return false;
		}
	}

	public static boolean loadSavedConfirmationData(Context context) {
		Log.i("Loading saved confirmation data...");
		try {
			JSONObject data = new JSONObject(IoUtils.readStringFromFile(ConfirmationUtils.CONFIRMATION_DATA_FILE,
					context));
			Db.setSearchParams((SearchParams) JSONUtils.getJSONable(data, Codes.SEARCH_PARAMS, SearchParams.class));
			Db.setSelectedProperty((Property) JSONUtils.getJSONable(data, Codes.PROPERTY, Property.class));
			Db.setSelectedRate((Rate) JSONUtils.getJSONable(data, Codes.RATE, Rate.class));
			Db.setBillingInfo((BillingInfo) JSONUtils.getJSONable(data, Codes.BILLING_INFO, BillingInfo.class));
			Db.setBookingResponse((BookingResponse) JSONUtils.getJSONable(data, Codes.BOOKING_RESPONSE,
					BookingResponse.class));
			return true;
		}
		catch (Exception e) {
			Log.e("Could not load hotel confirmation data state.", e);
			return false;
		}
	}

	public static boolean hasSavedConfirmationData(Context context) {
		File savedConfResults = context.getFileStreamPath(ConfirmationUtils.CONFIRMATION_DATA_FILE);
		return savedConfResults.exists();
	}

	public static boolean deleteSavedConfirmationData(Context context) {
		Log.i("Deleting saved confirmation data.");
		File savedConfResults = context.getFileStreamPath(ConfirmationUtils.CONFIRMATION_DATA_FILE);
		return savedConfResults.delete();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Miscellaneous

	public static void determineCancellationPolicy(Rate rate, View view) {
		Policy cancellationPolicy = rate.getRateRules().getPolicy(Policy.TYPE_CANCEL);

		TextView cancellationPolicyView = (TextView) view.findViewById(R.id.cancellation_policy_text_view);
		if (cancellationPolicyView != null) {
			if (cancellationPolicy != null) {
				cancellationPolicyView.setText(Html.fromHtml(cancellationPolicy.getDescription()));
			}
			else {
				cancellationPolicyView.setVisibility(View.GONE);
			}
		}

		TextView rulesRestrictionsTitle = (TextView) view.findViewById(R.id.rules_and_restrictions_title_text_view);
		if (rulesRestrictionsTitle != null) {
			rulesRestrictionsTitle.setVisibility((cancellationPolicy != null) ? View.VISIBLE : View.GONE);
		}
	}

	public static String determineContactText(Context context) {
		return context.getString(R.string.contact_phone_template, SupportUtils.getInfoSupportNumber(context));
	}

	public static void configureContactView(Context context, TextView contactView, String contactText) {
		if (!AndroidUtils.hasTelephonyFeature(context)) {
			contactView.setAutoLinkMask(0);
		}
		else {
			contactView.setAutoLinkMask(Linkify.PHONE_NUMBERS);
		}

		contactView.setText(contactText);
	}
}
