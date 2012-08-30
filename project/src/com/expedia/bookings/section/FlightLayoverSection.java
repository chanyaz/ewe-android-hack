package com.expedia.bookings.section;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Layover;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightLayoverSection extends LinearLayout {

	private TextView mLayoverTextView;

	public FlightLayoverSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mLayoverTextView = Ui.findView(this, R.id.layover_text_view);
	}

	public void bind(Flight flight1, Flight flight2) {
		Resources r = getResources();
		Layover layover = new Layover(flight1, flight2);
		String duration = DateTimeUtils.formatDuration(r, layover.mDuration);
		String waypoint = StrUtils.formatWaypoint(flight2.mOrigin);
		mLayoverTextView.setText(Html.fromHtml(r.getString(R.string.layover_duration_location_TEMPLATE, duration,
				waypoint)));
	}
}
