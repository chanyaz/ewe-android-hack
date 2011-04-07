package com.expedia.bookings.utils;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.hotellib.data.Money;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.data.RateBreakdown;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.utils.StrUtils;

public class LayoutUtils {

	public static void addRateDetails(Context context, ViewGroup detailsLayout, SearchParams searchParams,
			Property property,
			Rate rate) {
		addDetail(context, detailsLayout, R.string.room_type, rate.getRoomDescription());

		addDetail(context, detailsLayout, R.string.GuestsLabel, StrUtils.formatGuests(context, searchParams));

		DateFormat medDf = android.text.format.DateFormat.getMediumDateFormat(context);
		String start = medDf.format(searchParams.getCheckInDate().getTime());
		String end = medDf.format(searchParams.getCheckOutDate().getTime());
		int numDays = (int) Math.round((searchParams.getCheckOutDate().getTimeInMillis() - searchParams
				.getCheckInDate().getTimeInMillis()) / (1000 * 60 * 60 * 24));
		String numNights = (numDays == 1) ? context.getString(R.string.stay_duration_one_night) : context.getString(
				R.string.stay_duration_template, numDays);
		addDetail(context, detailsLayout, R.string.CheckIn, start);
		addDetail(context, detailsLayout, R.string.CheckOut, end + "\n" + numNights);

		// If there's a breakdown list, show that; otherwise, show the nightly mRate
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
		if (rate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : rate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String dateStr = dateFormat.format(date);
				addDetail(context, detailsLayout, context.getString(R.string.room_rate_template, dateStr), breakdown
						.getAmount().getFormattedMoney());
			}
		}
		else if (rate.getDailyAmountBeforeTax() != null) {
			addDetail(context, detailsLayout, R.string.RatePerRoomPerNight, rate.getDailyAmountBeforeTax()
					.getFormattedMoney());
		}

		Money taxesAndFeesPerRoom = rate.getTaxesAndFeesPerRoom();
		if (taxesAndFeesPerRoom != null && taxesAndFeesPerRoom.getFormattedMoney() != null
				&& taxesAndFeesPerRoom.getFormattedMoney().length() > 0) {
			addDetail(context, detailsLayout, R.string.TaxesAndFees, taxesAndFeesPerRoom.getFormattedMoney());
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
