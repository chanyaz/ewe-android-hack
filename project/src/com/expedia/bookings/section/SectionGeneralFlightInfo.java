package com.expedia.bookings.section;

import java.util.Calendar;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightTrip;
import com.mobiata.android.util.Ui;

public class SectionGeneralFlightInfo extends LinearLayout {

	//private Calendar mDepartureDate;
	private int mNumberOfTravlers;
	private FlightTrip mTrip;

	private TextView mDepartureDateTextView;
	private TextView mTravelerCountTextView;

	public SectionGeneralFlightInfo(Context context) {
		super(context);
	}

	public SectionGeneralFlightInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SectionGeneralFlightInfo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		// Cache views
		mDepartureDateTextView = Ui.findView(this, R.id.departure_date_long_form);
		mTravelerCountTextView = Ui.findView(this, R.id.traveler_count);
	}

	public void bind(FlightTrip trip, int numTravelers) {

		if (trip != null) {
			mTrip = trip;
			Calendar depDate = mTrip.getLeg(0).getFirstWaypoint().getMostRelevantDateTime();
			Calendar retDate = mTrip.getLeg(mTrip.getLegCount() - 1).getLastWaypoint().getMostRelevantDateTime();
			mNumberOfTravlers = numTravelers;

			if (mDepartureDateTextView != null) {
				String dateRange = DateUtils.formatDateRange(getContext(), depDate.getTimeInMillis(),
						retDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
								| DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH);
				mDepartureDateTextView.setText(dateRange);
			}
		}

		if (mTravelerCountTextView != null) {
			mTravelerCountTextView.setText(getResources().getQuantityString(R.plurals.number_of_travelers_TEMPLATE,
					mNumberOfTravlers, mNumberOfTravlers));
		}
	}

}
