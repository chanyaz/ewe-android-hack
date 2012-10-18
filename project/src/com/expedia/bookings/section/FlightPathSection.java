package com.expedia.bookings.section;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;

public class FlightPathSection extends LinearLayout {

	private ImageView mIconImageView;
	private TextView mInfoTextView;

	public FlightPathSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mIconImageView = Ui.findView(this, R.id.icon_image_view);

		mInfoTextView = Ui.findView(this, R.id.info_text_view);
		mInfoTextView.setTypeface(FontCache.getTypeface(Font.ROBOTO_LIGHT));
	}

	public void bind(Flight flight, boolean isDeparting) {
		if (isDeparting) {
			mInfoTextView.setText(getResources().getString(R.string.depart_from_TEMPLATE,
					StrUtils.formatWaypoint(flight.mOrigin)));

			mIconImageView.setImageResource(R.drawable.ic_departure_arrow_small);
		}
		else {
			mInfoTextView.setText(getResources().getString(R.string.arrive_at_TEMPLATE,
					StrUtils.formatWaypoint(flight.mDestination)));

			mIconImageView.setImageResource(R.drawable.ic_return_arrow_small);
		}
	}
}
