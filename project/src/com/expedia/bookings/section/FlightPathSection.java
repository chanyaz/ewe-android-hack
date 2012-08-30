package com.expedia.bookings.section;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;

public class FlightPathSection extends LinearLayout {

	private TextView mOriginTextView;
	private TextView mDestinationTextView;

	public FlightPathSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mOriginTextView = Ui.findView(this, R.id.origin_text_view);
		mDestinationTextView = Ui.findView(this, R.id.destination_text_view);
	}

	public void bind(Flight flight) {
		mOriginTextView.setText(StrUtils.formatWaypoint(flight.mOrigin));
		mDestinationTextView.setText(StrUtils.formatWaypoint(flight.mDestination));
	}
}
