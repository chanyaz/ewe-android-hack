package com.expedia.bookings.section;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.mobiata.android.util.Ui;

public class FlightSegmentSection extends LinearLayout {

	private FlightLegSummarySection mFlightLegSummary;


	public FlightSegmentSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mFlightLegSummary = Ui.findView(this, R.id.flight_leg_summary);
	}

	public void bind(FlightTripLeg tripLeg) {
		// Bind to views
		FlightTrip trip = tripLeg.getFlightTrip();
		FlightLeg leg = tripLeg.getFlightLeg();

		mFlightLegSummary.bind(trip, leg);
	}

}
