package com.expedia.bookings.section;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.widget.FlightTripView;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightLegSummarySection extends RelativeLayout {

	private static final DecimalFormat sDaySpanFormatter = new DecimalFormat("#");

	static {
		// TODO: Should this be localized in some way?
		sDaySpanFormatter.setPositivePrefix("+");
	}

	private TextView mAirlineTextView;
	private TextView mPriceTextView;
	private TextView mDepartureTimeTextView;
	private TextView mArrivalTimeTextView;
	private TextView mMultiDayTextView;
	private FlightTripView mFlightTripView;

	public FlightLegSummarySection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mAirlineTextView = Ui.findView(this, R.id.airline_text_view);
		mPriceTextView = Ui.findView(this, R.id.price_text_view);
		mDepartureTimeTextView = Ui.findView(this, R.id.departure_time_text_view);
		mArrivalTimeTextView = Ui.findView(this, R.id.arrival_time_text_view);
		mMultiDayTextView = Ui.findView(this, R.id.multi_day_text_view);
		mFlightTripView = Ui.findView(this, R.id.flight_trip_view);
	}

	public void bind(FlightTrip trip, FlightLeg leg) {
		bind(trip, leg, null, null);
	}

	public void bind(FlightTrip trip, FlightLeg leg, Calendar minTime, Calendar maxTime) {
		mAirlineTextView.setText(leg.getAirlinesFormatted());
		mDepartureTimeTextView.setText(formatTime(leg.getSegment(0).mOrigin.getMostRelevantDateTime()));
		mArrivalTimeTextView.setText(formatTime(leg.getSegment(leg.getSegmentCount() - 1).mDestination
				.getMostRelevantDateTime()));

		if (trip.hasPricing()) {
			mPriceTextView.setText(trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));
		}
		else {
			mPriceTextView.setText(null);
		}

		int daySpan = leg.getDaySpan();
		if (daySpan != 0) {
			mMultiDayTextView.setVisibility(View.VISIBLE);
			String daySpanStr = sDaySpanFormatter.format(daySpan);
			mMultiDayTextView.setText(getResources().getQuantityString(R.plurals.day_span, daySpan, daySpanStr));
		}
		else {
			mMultiDayTextView.setVisibility(View.INVISIBLE);
		}

		mFlightTripView.setUp(leg, minTime, maxTime);
	}

	private String formatTime(Calendar cal) {
		DateFormat df = android.text.format.DateFormat.getTimeFormat(getContext());
		return df.format(DateTimeUtils.getTimeInLocalTimeZone(cal));
	}
}
