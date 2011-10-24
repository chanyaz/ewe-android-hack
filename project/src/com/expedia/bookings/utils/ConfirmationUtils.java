package com.expedia.bookings.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Policy;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.SearchParams;
import com.mobiata.android.SocialUtils;

public class ConfirmationUtils {

	public static void share(Context context, SearchParams searchParams, Property property,
			BookingResponse bookingResponse, BillingInfo billingInfo, Rate rate, String contactText) {
		Resources res = context.getResources();

		DateFormat dateFormatter = new SimpleDateFormat("MM/dd");
		DateFormat fullDateFormatter = android.text.format.DateFormat.getMediumDateFormat(context);
		DateFormat dayFormatter = new SimpleDateFormat("EEE");

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

		appendLabelValue(context, body, R.string.confirmation_number, bookingResponse.getConfNumber());
		body.append("\n");
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

		Money surcharge = rate.getSurcharge();
		Money extraGuestFee = rate.getExtraGuestFee();
		if (extraGuestFee != null) {
			appendLabelValue(context, body, R.string.extra_guest_charge, extraGuestFee.getFormattedMoney());
			if (surcharge != null) {
				surcharge = surcharge.copy();
				surcharge.subtract(extraGuestFee);
			}
		}
		if (surcharge != null) {
			appendLabelValue(context, body, R.string.TaxesAndFees, surcharge.getFormattedMoney());
			body.append("\n");
		}

		if (rate.getTotalAmountAfterTax() != null) {
			body.append("\n");
			appendLabelValue(context, body, R.string.Total, rate.getTotalAmountAfterTax().getFormattedMoney());
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
