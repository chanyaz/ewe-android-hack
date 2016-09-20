package com.expedia.bookings.section;

import org.joda.time.DateTime;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.util.Ui;

public class SectionFlightLeg extends LinearLayout {

	private FlightTripLeg mTripLeg;

	private TextView mInfoTextView;
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

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mInfoTextView = Ui.findView(this, R.id.info_text_view);
		mFlightLegSummary = Ui.findView(this, R.id.flight_leg_summary);

		FontCache.setTypeface(mInfoTextView, Font.ROBOTO_LIGHT);
	}

	public void bind(FlightTripLeg tripLeg) {
		// Update internal data
		mTripLeg = tripLeg;

		// Bind to views
		FlightLeg leg = tripLeg.getFlightLeg();

		mFlightLegSummary.bind(leg);

		setInfoText(leg);
	}

	public void setInfoText(FlightLeg leg) {
		DateTime cal = leg.getFirstWaypoint().getMostRelevantDateTime().toLocalDateTime().toDateTime();//We always label with the day the flight (leg) departs
		long time = cal == null ? 0 : cal.getMillis();
		String formattedDate = DateUtils.formatDateTime(getContext(), time, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_SHOW_WEEKDAY
				| DateUtils.FORMAT_ABBREV_ALL);
		String formatted = getResources().getString(R.string.trip_to_with_date, formattedDate,
				StrUtils.getWaypointLocalizedCityOrCode(leg.getFirstWaypoint()),
				StrUtils.getWaypointLocalizedCityOrCode(leg.getLastWaypoint()));
		mInfoTextView.setText(formatted);
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience methods

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
