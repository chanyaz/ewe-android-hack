package com.expedia.bookings.widget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.expedia.bookings.R;
import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.utils.Ui;

public class EventSummaryView extends LinearLayout {

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private TextView mTimeTextView;
	private TextView mDateTextView;
	private TextView mLocationtextView;
	private ImageButton mLocationMapImageButton;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public EventSummaryView(Context context) {
		super(context);
		init(context);
	}

	public EventSummaryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public EventSummaryView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void bind(DateTime dateTime, final Location location, final boolean directions, String vendorName) {
		Context context = getContext();
		mTimeTextView.setText(dateTime.formatTime(context, DateUtils.FORMAT_SHOW_TIME));
		mDateTextView.setText(dateTime.formatTime(context, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
				| DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH));
		mLocationtextView.setText(String.format(context.getString(R.string.car_rental_agency_name_location),
				vendorName, location.getCity(), location.getStateCode()));
		mLocationMapImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String param = directions ? "daddr" : "q";
				final Uri uri = Uri.parse("http://maps.google.com/maps?" + param + "=" + location.toLongFormattedString());
				final Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);

				getContext().startActivity(intent);
			}
		});
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void init(Context context) {
		setOrientation(HORIZONTAL);

		inflate(context, R.layout.widget_event_summary, this);

		mTimeTextView = Ui.findView(this, R.id.time_text_view);
		mDateTextView = Ui.findView(this, R.id.date_text_view);
		mLocationtextView = Ui.findView(this, R.id.location_text_view);
		mLocationMapImageButton = Ui.findView(this, R.id.location_map_image_button);
	}
}
