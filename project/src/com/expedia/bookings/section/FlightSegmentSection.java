package com.expedia.bookings.section;

import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;

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

	public void bind(Flight flight, Calendar minTime, Calendar maxTime) {
		mFlightLegSummary.bindFlight(flight, minTime, maxTime);
	}
}
