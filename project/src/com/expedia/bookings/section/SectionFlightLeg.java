package com.expedia.bookings.section;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.section.FlightLegSummarySection.FlightLegSummarySectionListener;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class SectionFlightLeg extends LinearLayout {

	private FlightTripLeg mTripLeg;

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

	public void setListener(FlightLegSummarySectionListener listener) {
		mFlightLegSummary.setListener(listener);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mArriveOrDepartWithDateTextView = Ui.findView(this, R.id.display_arrive_or_depart_with_date);
		mInboundOutboundArrow = Ui.findView(this, R.id.display_inbound_outbound_arrow);
		mFlightLegSummary = Ui.findView(this, R.id.flight_leg_summary);
	}

	public void bind(FlightTripLeg tripLeg) {
		// Update internal data
		mTripLeg = tripLeg;

		// Bind to views
		FlightTrip trip = tripLeg.getFlightTrip();
		FlightLeg leg = tripLeg.getFlightLeg();

		mFlightLegSummary.bind(trip, leg);

		String formatted = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("EE, MMM dd");
		String formattedDate = dateFormat.format((isOutbound() ? leg.getFirstWaypoint() : leg.getLastWaypoint())
				.getMostRelevantDateTime().getTime());
		formatted = String.format(getResources().getString(R.string.trip_to_with_date), formattedDate, leg
				.getFirstWaypoint().getAirport().mCity, leg.getLastWaypoint().getAirport().mCity);

		mArriveOrDepartWithDateTextView.setText(formatted);

		if (isOutbound()) {
			mInboundOutboundArrow.setImageResource(R.drawable.ic_departure_arrow);
		}
		else {
			mInboundOutboundArrow.setImageResource(R.drawable.ic_return_arrow);
		}
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
}
