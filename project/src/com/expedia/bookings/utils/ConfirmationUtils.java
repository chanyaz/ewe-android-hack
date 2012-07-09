package com.expedia.bookings.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Policy;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.tracking.TrackingUtils;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;

public class ConfirmationUtils {
	public static final String CONFIRMATION_DATA_FILE = "confirmation.dat";
	public static final String CONFIRMATION_DATA_VERSION_FILE = "confirmation-version.dat";

	public static void share(Context context, SearchParams searchParams, Property property,
			BookingResponse bookingResponse, BillingInfo billingInfo, Rate rate, Rate discountRate, String contactText) {
		Resources res = context.getResources();

		DateFormat dateFormatter = new SimpleDateFormat("MM/dd");
		dateFormatter.setTimeZone(CalendarUtils.getFormatTimeZone());
		DateFormat fullDateFormatter = android.text.format.DateFormat.getMediumDateFormat(context);
		fullDateFormatter.setTimeZone(CalendarUtils.getFormatTimeZone());
		DateFormat dayFormatter = new SimpleDateFormat("EEE");
		dayFormatter.setTimeZone(CalendarUtils.getFormatTimeZone());

		Date checkIn = searchParams.getCheckInDate().getTime();
		Date checkOut = searchParams.getCheckOutDate().getTime();

		// Create the subject
		String dateStart = dateFormatter.format(checkIn);
		String dateEnd = dateFormatter.format(checkOut);
		String subject = context.getString(R.string.share_subject_template, property.getName(), dateStart, dateEnd);

		// Create the body 
		StringBuilder body = new StringBuilder();
		body.append(context.getString(R.string.share_body_start));
		body.append("\n\n");

		body.append(property.getName());
		body.append("\n");
		body.append(StrUtils.formatAddress(property.getLocation()));
		body.append("\n\n");

		if (!TextUtils.isEmpty(bookingResponse.getHotelConfNumber())) {
			appendLabelValue(context, body, R.string.confirmation_number, bookingResponse.getHotelConfNumber());
			body.append("\n");
		}
		appendLabelValue(context, body, R.string.itinerary_number, bookingResponse.getItineraryId());
		body.append("\n\n");

		appendLabelValue(context, body, R.string.name,
				context.getString(R.string.name_template, billingInfo.getFirstName(), billingInfo.getLastName()));
		body.append("\n");
		appendLabelValue(context, body, R.string.CheckIn,
				dayFormatter.format(checkIn) + ", " + fullDateFormatter.format(checkIn));
		body.append("\n");
		appendLabelValue(context, body, R.string.CheckOut,
				dayFormatter.format(checkOut) + ", " + fullDateFormatter.format(checkOut));
		body.append("\n");
		int numDays = searchParams.getStayDuration();
		appendLabelValue(context, body, R.string.stay_duration,
				res.getQuantityString(R.plurals.length_of_stay, numDays, numDays));
		body.append("\n\n");

		appendLabelValue(context, body, R.string.room_type, Html.fromHtml(rate.getRoomDescription()).toString());
		body.append("\n");
		appendLabelValue(context, body, R.string.bed_type, rate.getRatePlanName());
		body.append("\n");
		appendLabelValue(context, body, R.string.adults, searchParams.getNumAdults() + "");
		body.append("\n");
		appendLabelValue(context, body, R.string.children, searchParams.getNumChildren() + "");
		body.append("\n\n");

		if (rate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : rate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String dateStr = dayFormatter.format(date) + ", " + fullDateFormatter.format(date);
				Money amount = breakdown.getAmount();
				if (amount.getAmount() == 0) {
					appendLabelValue(body, context.getString(R.string.room_rate_template, dateStr),
							context.getString(R.string.free));
				}
				else {
					appendLabelValue(body, context.getString(R.string.room_rate_template, dateStr),
							amount.getFormattedMoney());
				}
				body.append("\n");
			}
			body.append("\n\n");
		}

		if (rate.getTotalAmountBeforeTax() != null) {
			appendLabelValue(context, body, R.string.subtotal, rate.getTotalAmountBeforeTax().getFormattedMoney());
			body.append("\n");
		}

		Money totalSurcharge = new Money(rate.getTotalSurcharge());
		Money extraGuestFee = rate.getExtraGuestFee();
		if (extraGuestFee != null) {
			appendLabelValue(context, body, R.string.extra_guest_charge, extraGuestFee.getFormattedMoney());
			body.append("\n");
			if (totalSurcharge != null) {
				totalSurcharge = totalSurcharge.copy();
				totalSurcharge.subtract(extraGuestFee);
			}
		}
		if (totalSurcharge != null) {
			appendLabelValue(context, body, R.string.TaxesAndFees, totalSurcharge.getFormattedMoney());
			body.append("\n");
		}

		if (discountRate != null) {
			Money discount = new Money(discountRate.getTotalAmountAfterTax());
			discount.subtract(rate.getTotalAmountAfterTax());
			appendLabelValue(context, body, R.string.discount, discount.getFormattedMoney());
			body.append("\n");
			appendLabelValue(context, body, R.string.Total, discountRate.getTotalAmountAfterTax().getFormattedMoney());
			body.append("\n");
		}
		else {
			if (rate.getTotalAmountAfterTax() != null) {
				body.append("\n");
				appendLabelValue(context, body, R.string.Total, rate.getTotalAmountAfterTax().getFormattedMoney());
			}
		}

		Policy cancellationPolicy = rate.getRateRules().getPolicy(Policy.TYPE_CANCEL);
		if (cancellationPolicy != null) {
			body.append("\n\n");
			body.append(context.getString(R.string.cancellation_policy));
			body.append("\n");
			body.append(Html.fromHtml(cancellationPolicy.getDescription()));
		}

		body.append("\n\n");
		body.append(contactText);

		SocialUtils.email(context, subject, body.toString());

		// Track the share
		Log.d("Tracking \"CKO.CP.ShareBooking\" onClick");
		TrackingUtils.trackSimpleEvent(context, null, null, "Shopper", "CKO.CP.ShareBooking");
	}

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
			Log.e("Could not save ConfirmationActivity state.", e);
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

	public static Intent generateIntentToShowPropertyOnMap(Property property) {
		Intent newIntent = new Intent(Intent.ACTION_VIEW);
		String queryAddress = StrUtils.formatAddress(property.getLocation()).replace("\n", " ");
		newIntent.setData(Uri.parse("geo:0,0?q=" + queryAddress));
		return newIntent;
	}

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

	private static void appendLabelValue(Context context, StringBuilder sb, int labelStrId, String value) {
		appendLabelValue(sb, context.getString(labelStrId), value);
	}

	private static void appendLabelValue(StringBuilder sb, String label, String value) {
		sb.append(label);
		sb.append(": ");
		sb.append(value);
	}

}
