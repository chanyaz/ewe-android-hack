package com.expedia.bookings.section;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;

public class InfoBarSection extends LinearLayout {

	private static final int SHOW_URGENCY_CUTOFF = 5;

	private TextView mLeftTextView;
	private TextView mRightTextView;

	public InfoBarSection(Context context) {
		super(context);
	}

	public InfoBarSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public InfoBarSection(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mLeftTextView = Ui.findView(this, R.id.left_text_view);
		mRightTextView = Ui.findView(this, R.id.right_text_view);

		// Configur font
		Typeface font = FontCache.getTypeface(Font.ROBOTO_LIGHT);
		mLeftTextView.setTypeface(font);
		mRightTextView.setTypeface(font);
	}

	public void bindFlightDetails(FlightTrip trip, FlightLeg leg) {
		Context context = getContext();

		// Bind left label (distance)
		String duration = DateTimeUtils.formatDuration(getResources(), (int) (leg.getDuration() / 60000));
		int distanceInMiles = leg.getDistanceInMiles();
		if (distanceInMiles <= 0) {
			mLeftTextView.setText(Html.fromHtml(context.getString(R.string.bold_template, duration)));
		}
		else {
			String distance = FormatUtils.formatDistance(context, leg.getDistanceInMiles());
			mLeftTextView.setText(Html.fromHtml(context.getString(R.string.time_distance_TEMPLATE, duration,
					distance)));
		}

		// Bind right label (booking price)
		String fare = trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL);
		int seatsRemaining = trip.getSeatsRemaining();
		if (seatsRemaining <= SHOW_URGENCY_CUTOFF) {
			String urgencyStr = getResources().getQuantityString(R.plurals.urgency_book_TEMPLATE, seatsRemaining,
					seatsRemaining, fare);
			mRightTextView.setText(Html.fromHtml(urgencyStr));
		}
		else {
			int bookNowResId;
			if (trip.getLegCount() == 1) {
				bookNowResId = R.string.one_way_price_TEMPLATE;
			}
			else {
				if (trip.getLeg(0).equals(leg)) {
					bookNowResId = R.string.round_trip_price_TEMPLATE;
				}
				else {
					bookNowResId = R.string.book_now_price_TEMPLATE;
				}
			}

			mRightTextView.setText(Html.fromHtml(context.getString(bookNowResId, fare)));
		}
	}

	public void bindTripOverview(FlightTrip trip, int numTravelers) {
		// Bind left label (trip dates)
		Calendar depDate = trip.getLeg(0).getFirstWaypoint().getMostRelevantDateTime();
		Calendar retDate = trip.getLeg(trip.getLegCount() - 1).getLastWaypoint().getMostRelevantDateTime();

		long start = DateTimeUtils.getTimeInLocalTimeZone(depDate).getTime();
		long end = DateTimeUtils.getTimeInLocalTimeZone(retDate).getTime();

		String dateRange = DateUtils.formatDateRange(getContext(), start, end,
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY
						| DateUtils.FORMAT_ABBREV_MONTH);
		mLeftTextView.setText(dateRange);

		// Bind right label (# travelers)
		mRightTextView.setText(getResources().getQuantityString(R.plurals.number_of_travelers_TEMPLATE,
				numTravelers, numTravelers));
	}
}
