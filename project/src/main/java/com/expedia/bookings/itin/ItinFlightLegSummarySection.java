package com.expedia.bookings.itin;

import java.text.DecimalFormat;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightTripView;
import com.mobiata.flightlib.data.Airline;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;
import com.squareup.phrase.Phrase;

public class ItinFlightLegSummarySection extends RelativeLayout {

	private static final DecimalFormat sDaySpanFormatter = new DecimalFormat("#");

	static {
		sDaySpanFormatter.setPositivePrefix("+");
	}

	private TextView mAirlineTextView;
	private ImageView mOperatingCarrierImageView;
	private TextView mOperatingCarrierTextView;
	private TextView mDepartureTimeTextView;
	private TextView mArrivalTimeTextView;
	private TextView mMultiDayTextView;
	private FlightTripView mFlightTripView;

	public ItinFlightLegSummarySection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mAirlineTextView = Ui.findView(this, R.id.airline_text_view);
		mOperatingCarrierImageView = Ui.findView(this, R.id.operating_carrier_image_view);
		mOperatingCarrierTextView = Ui.findView(this, R.id.operating_carrier_text_view);
		mDepartureTimeTextView = Ui.findView(this, R.id.departure_time_text_view);
		mArrivalTimeTextView = Ui.findView(this, R.id.arrival_time_text_view);
		mMultiDayTextView = Ui.findView(this, R.id.multi_day_text_view);
		mFlightTripView = Ui.findView(this, R.id.flight_trip_view);
	}

	public void bindFlight(Flight flight, DateTime minTime, DateTime maxTime) {
		// Fake a flight leg
		FlightLeg pseudoLeg = new FlightLeg();
		pseudoLeg.addSegment(flight);

		bind(pseudoLeg, minTime, maxTime);
	}

	private void bind(final FlightLeg leg, DateTime minTime, DateTime maxTime) {
		Context context = getContext();
		Resources res = getResources();

		Flight firstFlight = leg.getSegment(0);
		String departureAirport = (leg.getFirstWaypoint().getAirport().mName != null) ? leg.getFirstWaypoint().getAirport().mName : "" ;
		String arrivalAirport = (leg.getLastWaypoint().getAirport().mName != null) ? leg.getLastWaypoint().getAirport().mName : "";

		if (mAirlineTextView != null) {
			mAirlineTextView.setText(getAirlinesStr(context, firstFlight));
		}

		if (mDepartureTimeTextView != null) {
			mDepartureTimeTextView.setText(formatTime(leg.getFirstWaypoint().getBestSearchDateTime()));
		}

		if (mArrivalTimeTextView != null) {
			mArrivalTimeTextView.setText(formatTime(leg.getLastWaypoint().getBestSearchDateTime()));
		}

		if (mMultiDayTextView != null) {
			int daySpan = leg.getDaySpan();
			if (daySpan != 0) {
				mMultiDayTextView.setVisibility(View.VISIBLE);
				String daySpanStr = sDaySpanFormatter.format(daySpan);
				mMultiDayTextView.setText(res.getQuantityString(R.plurals.day_span, Math.abs(daySpan), daySpanStr));
			}
			else {
				mMultiDayTextView.setVisibility(View.INVISIBLE);
			}
		}

		mFlightTripView.setUp(leg, minTime, maxTime);

		adjustLayout(leg);
		this.setContentDescription(Phrase.from(context, R.string.flight_overview_TEMPLATE_cont_desc)
			.put("airline", getAirlinesStr(context, firstFlight))
			.put("departuretime", formatTime(leg.getFirstWaypoint().getBestSearchDateTime()))
			.put("arrivaltime", formatTime(leg.getLastWaypoint().getBestSearchDateTime()))
			.put("departureairport", departureAirport)
			.put("arrivalairport", arrivalAirport)
			.format().toString());
	}

	private static String getAirlinesStr(Context context, Flight flight) {
		return FormatUtils.formatFlightNumber(flight, context);
	}

	private void adjustLayout(final FlightLeg leg) {
		Context context = getContext();
		Flight firstFlight = leg.getSegment(0);

		int belowTarget;
		FlightCode primaryFlightCode = firstFlight.getPrimaryFlightCode();
		FlightCode opFlightCode = firstFlight.getOperatingFlightCode();
		boolean hasDifferentOperatingCarrierCode = (primaryFlightCode != null) && !primaryFlightCode.equals(opFlightCode);
		if (hasDifferentOperatingCarrierCode) {
			String operatedBy;
			if (!TextUtils.isEmpty(opFlightCode.mAirlineCode)) {
				Airline airline = Db.getAirline(opFlightCode.mAirlineCode);
				operatedBy = FormatUtils.formatAirline(airline, context);
			}
			else {
				operatedBy = opFlightCode.mAirlineName;
			}

			mOperatingCarrierTextView.setText(context.getString(R.string.operated_by_TEMPLATE, operatedBy));

			mOperatingCarrierImageView.setVisibility(View.VISIBLE);
			mOperatingCarrierTextView.setVisibility(View.VISIBLE);

			belowTarget = mOperatingCarrierTextView.getId();
		}
		else {
			belowTarget = mAirlineTextView.getId();
		}

		// Adjust rules depending on what we need to be below
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mDepartureTimeTextView.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, belowTarget);
		params = (RelativeLayout.LayoutParams) mArrivalTimeTextView.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, belowTarget);
		params = (RelativeLayout.LayoutParams) mFlightTripView.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, belowTarget);

		if (mMultiDayTextView != null) {
			// 104 - modify card size depending on whether or not this is string is displayed to prevent overlap
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mMultiDayTextView.getLayoutParams();
			Resources res = getResources();
			int rightMargin = res.getDimensionPixelSize(R.dimen.flight_leg_day_textview_margin_right);
			int topMargin = res.getDimensionPixelSize(leg.getDaySpan() == 0
				? R.dimen.flight_leg_day_textview_margin_top
				: R.dimen.flight_leg_day_textview_margin_top_no_overlap);
			lp.setMargins(0, topMargin, rightMargin, 0);
		}
	}

	private String formatTime(DateTime cal) {
		String dateFormat = DateTimeUtils.getDeviceTimeFormat(getContext());
		return JodaUtils.format(cal, dateFormat);
	}
}
