package com.expedia.bookings.section;

import java.text.DateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightDetailsActivity;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.Money;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class SectionFlightLeg extends LinearLayout {

	private FlightTripLeg mTripLeg;

	private SectionFlightLegListener mListener;

	private Button mDetailsButton;
	private Button mDeselectButton;
	private TextView mCarrierNameTextView;
	private TextView mDepartureTimeTextView;
	private TextView mArrivalTimeTextView;
	private TextView mArriveOrDepartWithDateTextView;
	private TextView mFlightPriceTextView;
	private ImageView mInboundOutboundArrow;

	public SectionFlightLeg(Context context) {
		super(context);
	}

	public SectionFlightLeg(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SectionFlightLeg(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setListener(SectionFlightLegListener listener) {
		mListener = listener;
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mDetailsButton = Ui.findView(this, R.id.details_btn);
		mDeselectButton = Ui.findView(this, R.id.deselect_btn);
		mCarrierNameTextView = Ui.findView(this, R.id.display_carrier_name);
		mDepartureTimeTextView = Ui.findView(this, R.id.display_departure_time);
		mArrivalTimeTextView = Ui.findView(this, R.id.display_arrival_time);
		mArriveOrDepartWithDateTextView = Ui.findView(this, R.id.display_arrive_or_depart_with_date);
		mFlightPriceTextView = Ui.findView(this, R.id.display_flight_price);
		mInboundOutboundArrow = Ui.findView(this, R.id.display_inbound_outbound_arrow);

		// Setup click listeners once
		mDetailsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = getContext();
				Intent intent = new Intent(context, FlightDetailsActivity.class);
				intent.putExtra(FlightDetailsActivity.EXTRA_TRIP_KEY, mTripLeg.getFlightTrip().getProductKey());
				intent.putExtra(FlightDetailsActivity.EXTRA_LEG_POSITION, getLegPosition());
				context.startActivity(intent);
			}
		});

		mDeselectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onDeselect();
				}
			}
		});
	}

	public void bind(FlightTripLeg tripLeg, boolean deselectButtonEnabled) {
		// Update internal data
		mTripLeg = tripLeg;

		// Bind to views
		FlightTrip trip = tripLeg.getFlightTrip();
		FlightLeg leg = tripLeg.getFlightLeg();

		mDeselectButton.setVisibility(deselectButtonEnabled ? View.VISIBLE : View.GONE);
		mCarrierNameTextView.setText(leg.getAirlinesFormatted());
		mDepartureTimeTextView.setText(getFormatedRelevantWaypointTime(leg.getFirstWaypoint()));
		mArrivalTimeTextView.setText(getFormatedRelevantWaypointTime(leg.getLastWaypoint()));
		mFlightPriceTextView.setText(trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));

		// Arrival/departure time formatted display
		String formatted = "";
		String formatString = "";
		Calendar cal = null;
		if (isOutbound()) {
			cal = leg.getFirstWaypoint().getMostRelevantDateTime();
			formatString = getResources().getString(R.string.departs_with_date_TEMPLATE);
		}
		else {
			cal = leg.getLastWaypoint().getMostRelevantDateTime();
			formatString = getResources().getString(R.string.arrives_with_date_TEMPLATE);
		}

		String shortMonth = DateUtils.getMonthString(cal.get(Calendar.MONTH), DateUtils.LENGTH_SHORT);
		String day = "" + cal.get(Calendar.DAY_OF_MONTH);
		formatted = String.format(formatString, shortMonth, day);
		mArriveOrDepartWithDateTextView.setText(formatted);

		// TODO: Handle mInboundOutboundArrow once we get assets
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience methods

	public String getFormatedRelevantWaypointTime(Waypoint wp) {
		Context context = getContext();
		DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
		return timeFormat.format(DateTimeUtils.getTimeInConfiguredTimeZone(context, wp.getMostRelevantDateTime()));
	}

	private int getLegPosition() {
		FlightTrip trip = mTripLeg.getFlightTrip();
		FlightLeg leg = mTripLeg.getFlightLeg();

		int legCount = trip.getLegCount();
		for (int a = 0; a < legCount; a++) {
			if (leg.equals(trip.getLeg(a))) {
				return a;
			}
		}

		// Should never get here
		return -1;
	}

	// Convenience method that works while we only support 1-2 legs.
	private boolean isOutbound() {
		return getLegPosition() == 0;
	}

	//////////////////////////////////////////////////////////////////////////
	// SectionFlightLegListener

	public interface SectionFlightLegListener {
		public void onDeselect();
	}
}
