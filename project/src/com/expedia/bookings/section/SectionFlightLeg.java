package com.expedia.bookings.section;

import java.text.DateFormat;
import java.util.Calendar;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class SectionFlightLeg extends LinearLayout {

	private FlightTripLeg mTripLeg;

	private SectionFlightLegListener mListener;

	private Button mDeselectButton;
	private TextView mArriveOrDepartWithDateTextView;
	private ImageView mInboundOutboundArrow;
	private FlightLegSummarySection mFlightLegSummary;

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
		mDeselectButton = Ui.findView(this, R.id.deselect_btn);
		mArriveOrDepartWithDateTextView = Ui.findView(this, R.id.display_arrive_or_depart_with_date);
		mInboundOutboundArrow = Ui.findView(this, R.id.display_inbound_outbound_arrow);
		mFlightLegSummary = Ui.findView(this, R.id.flight_leg_summary);

		// Setup click listeners once
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

		mFlightLegSummary.bind(trip, leg);

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
