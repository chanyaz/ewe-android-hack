package com.expedia.bookings.section;

import java.util.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightSegmentAttributes;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightSegmentSection extends LinearLayout {

	private FlightLegSummarySection mFlightLegSummary;
	private TextView mDetailsTextView;

	public FlightSegmentSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mFlightLegSummary = Ui.findView(this, R.id.flight_leg_summary);
		mDetailsTextView = Ui.findView(this, R.id.details_text_view);
	}

	public void bind(Flight flight, FlightSegmentAttributes attrs, Calendar minTime, Calendar maxTime) {
		Resources r = getResources();

		mFlightLegSummary.bindFlight(flight, minTime, maxTime);

		String duration = DateTimeUtils.formatDuration(r, flight.getTripTime());
		String cabin = r.getString(attrs.getCabinCode().getResId());

		if (TextUtils.isEmpty(flight.mAircraftType)) {
			mDetailsTextView.setText(Html.fromHtml(r.getString(R.string.flight_details_no_plane_info_TEMPLATE,
					duration, cabin, attrs.getBookingCode())));
		}
		else {
			mDetailsTextView.setText(Html.fromHtml(r.getString(R.string.flight_details_TEMPLATE, duration, cabin,
					attrs.getBookingCode(), flight.mAircraftType)));
		}
	}
}
