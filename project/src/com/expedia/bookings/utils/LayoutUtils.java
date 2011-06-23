package com.expedia.bookings.utils;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.hotellib.data.Money;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.data.RateBreakdown;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.utils.StrUtils;

public class LayoutUtils {

	public static void configureHeader(Activity activity, Property property, OnClickListener onBookNowClick) {
		TextView name = (TextView) activity.findViewById(R.id.name_text_view);
		name.setText(property.getName());
		RatingBar hotelRating = (RatingBar) activity.findViewById(R.id.hotel_rating_bar);
		hotelRating.setRating((float) property.getHotelRating());
		RatingBar tripAdvisorRating = (RatingBar) activity.findViewById(R.id.trip_advisor_rating_bar);
		tripAdvisorRating.setRating((float) property.getTripAdvisorRating());
		TextView location = (TextView) activity.findViewById(R.id.location_text_view);
		location.setText(StrUtils.formatAddress(property.getLocation(), StrUtils.F_CITY + StrUtils.F_STATE_CODE));

		Button bookButton = (Button) activity.findViewById(R.id.book_now_button);
		bookButton.setOnClickListener(onBookNowClick);
	}

	public static void addRateDetails(Context context, ViewGroup detailsLayout, SearchParams searchParams,
			Property property,
			Rate rate) {
		addDetail(context, detailsLayout, R.string.room_type, rate.getRoomDescription());

		addDetail(context, detailsLayout, R.string.bed_type, rate.getRatePlanName());

		addDetail(context, detailsLayout, R.string.GuestsLabel, StrUtils.formatGuests(context, searchParams));

		DateFormat medDf = android.text.format.DateFormat.getMediumDateFormat(context);
		String start = medDf.format(searchParams.getCheckInDate().getTime());
		String end = medDf.format(searchParams.getCheckOutDate().getTime());
		int numDays = (int) Math.round((searchParams.getCheckOutDate().getTimeInMillis() - searchParams
				.getCheckInDate().getTimeInMillis()) / (1000 * 60 * 60 * 24));
		addDetail(context, detailsLayout, R.string.CheckIn, start);
		addDetail(context, detailsLayout, R.string.CheckOut, end);
		addDetail(context, detailsLayout, R.string.stay_duration,
				context.getResources().getQuantityString(R.plurals.length_of_stay, numDays, numDays));

		// If there's a breakdown list, show that; otherwise, show the nightly mRate
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
		if (rate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : rate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String dateStr = dateFormat.format(date);
				Money amount = breakdown.getAmount();
				if (amount.getAmount() == 0) {
					addDetail(context, detailsLayout, context.getString(R.string.room_rate_template, dateStr),
							context.getString(R.string.free));
				}
				else {
					addDetail(context, detailsLayout, context.getString(R.string.room_rate_template, dateStr),
							breakdown.getAmount().getFormattedMoney());
				}
			}
		}
		else if (rate.getDailyAmountBeforeTax() != null) {
			addDetail(context, detailsLayout, R.string.RatePerRoomPerNight, rate.getDailyAmountBeforeTax()
					.getFormattedMoney());
		}

		Money totalSurcharge = rate.getSurcharge();
		Money extraGuestFee = rate.getExtraGuestFee();
		if (extraGuestFee != null) {
			addDetail(context, detailsLayout, R.string.extra_guest_charge, extraGuestFee.getFormattedMoney());
			if (totalSurcharge != null) {
				// Make a mutable copy
				totalSurcharge = totalSurcharge.copy();
				totalSurcharge.subtract(extraGuestFee);
			}
		}
		if (totalSurcharge != null) {
			addDetail(context, detailsLayout, R.string.TaxesAndFees, totalSurcharge.getFormattedMoney());
		}
	}

	public static void addDetail(Context context, ViewGroup parent, int labelStrId, String value) {
		addDetail(context, parent, context.getString(labelStrId), value);
	}

	public static void addDetail(Context context, ViewGroup parent, String label, String value) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View detailRow = inflater.inflate(R.layout.snippet_booking_detail, parent, false);
		TextView labelView = (TextView) detailRow.findViewById(R.id.label_text_view);
		labelView.setText(label);
		TextView valueView = (TextView) detailRow.findViewById(R.id.value_text_view);
		valueView.setText(value);
		parent.addView(detailRow);
	}
}
