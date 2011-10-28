package com.expedia.bookings.utils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.widget.RoomTypeHandler;

public class LayoutUtils {

	public static void configureHeader(Activity activity, Property property, OnClickListener onBookNowClick,
			OnClickListener onReviewsClick) {
		TextView name = (TextView) activity.findViewById(R.id.name_text_view);
		name.setText(property.getName());
		RatingBar userRating = (RatingBar) activity.findViewById(R.id.user_rating_bar);
		userRating.setRating((float) property.getAverageExpediaRating());
		TextView location = (TextView) activity.findViewById(R.id.location_text_view);
		location.setText(StrUtils.formatAddress(property.getLocation(), StrUtils.F_CITY + StrUtils.F_STATE_CODE));

		TextView reviewsText = (TextView) activity.findViewById(R.id.user_rating_text_view);
		int numReviews = property.getTotalReviews();
		reviewsText.setText(activity.getResources().getQuantityString(R.plurals.number_of_reviews, numReviews,
				numReviews));

		View reviewsContainer = activity.findViewById(R.id.user_rating_layout);
		if (onReviewsClick == null) {
			reviewsContainer.setEnabled(false);
		}
		else {
			reviewsContainer.setOnClickListener(onReviewsClick);
		}

		TextView bookButton = (TextView) activity.findViewById(R.id.book_now_button);
		bookButton.setOnClickListener(onBookNowClick);
	}

	public static void addRateDetails(Context context, ViewGroup detailsLayout, SearchParams searchParams,
			Property property, Rate rate, RoomTypeHandler roomTypeHandler) {
		View bedTypeRow = addDetail(context, detailsLayout, R.string.bed_type, rate.getRatePlanName());
		if (roomTypeHandler != null) {
			roomTypeHandler.addClickableView(bedTypeRow);
		}

		addDetail(context, detailsLayout, R.string.GuestsLabel, StrUtils.formatGuests(context, searchParams));

		String start = formatCheckInOutDate(context, searchParams.getCheckInDate());
		String end = formatCheckInOutDate(context, searchParams.getCheckOutDate());
		String timeLoader = "--:--";
		int numDays = searchParams.getStayDuration();
		addDetail(context, detailsLayout, context.getString(R.string.CheckIn),
				context.getString(R.string.check_in_out_time_template, timeLoader, start), R.id.check_in_time);
		addDetail(context, detailsLayout, context.getString(R.string.CheckOut),
				context.getString(R.string.check_in_out_time_template, timeLoader, end), R.id.check_out_time);
		addDetail(context, detailsLayout, R.string.stay_duration,
				context.getResources().getQuantityString(R.plurals.length_of_stay, numDays, numDays));
		addSpace(context, detailsLayout, 8);

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

	public static View addDetail(Context context, ViewGroup parent, int labelStrId, CharSequence value) {
		return addDetail(context, parent, context.getString(labelStrId), value, -1);
	}

	public static View addDetail(Context context, ViewGroup parent, CharSequence label, CharSequence value) {
		return addDetail(context, parent, label, value, -1);
	}

	public static View addDetail(Context context, ViewGroup parent, CharSequence label, CharSequence value, int valueId) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View detailRow = inflater.inflate(R.layout.snippet_booking_detail, parent, false);
		TextView labelView = (TextView) detailRow.findViewById(R.id.label_text_view);
		labelView.setText(label);
		TextView valueView = (TextView) detailRow.findViewById(R.id.value_text_view);
		valueView.setText(value);
		if (valueId != -1) {
			valueView.setId(valueId);
		}
		parent.addView(detailRow);

		return detailRow;
	}

	public static String formatCheckInOutDate(Context context, Calendar cal) {
		DateFormat medDf = android.text.format.DateFormat.getMediumDateFormat(context);
		medDf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return DateUtils.getDayOfWeekString(cal.get(Calendar.DAY_OF_WEEK), DateUtils.LENGTH_MEDIUM) + ", "
				+ medDf.format(cal.getTime());
	}

	public static void addSpace(Context context, ViewGroup parent, int spaceInDp) {
		int height = (int) context.getResources().getDisplayMetrics().density * spaceInDp;
		View v = new View(context);
		v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, height));
		parent.addView(v);
	}
}
