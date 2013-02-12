package com.expedia.bookings.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.utils.Ui;

public class EventSummaryView extends LinearLayout {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm a", Locale.getDefault());
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EE, MMM d, yyyy", Locale.getDefault());

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

	public void bind(Date date, final Location location) {
		mTimeTextView.setText(TIME_FORMAT.format(date));
		mDateTextView.setText(DATE_FORMAT.format(date));
		mLocationtextView.setText(location.toFormattedString());
		mLocationMapImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri
						.parse("http://maps.google.com/maps?q=" + location.toFormattedString()));

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