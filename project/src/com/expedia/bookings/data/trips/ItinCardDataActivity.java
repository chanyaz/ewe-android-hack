package com.expedia.bookings.data.trips;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.expedia.bookings.data.Activity;
import com.expedia.bookings.data.DateTime;

public class ItinCardDataActivity extends ItinCardData {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final SimpleDateFormat DETAIL_DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.getDefault());

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Activity mActivity;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCardDataActivity(TripComponent tripComponent) {
		super(tripComponent);
		mActivity = ((TripActivity) tripComponent).getActivity();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public String getTitle() {
		return mActivity.getTitle();
	}

	public DateTime getActiveDate() {
		return getTripComponent().getParentTrip().getStartDate();
	}

	public DateTime getExpirationDate() {
		return getTripComponent().getParentTrip().getEndDate();
	}

	public String getFormattedActiveDate() {
		return DETAIL_DATE_FORMAT.format(getActiveDate().getCalendar().getTime());
	}

	public String getFormattedExpirationDate() {
		return DETAIL_DATE_FORMAT.format(getExpirationDate().getCalendar().getTime());
	}

	public String getFormattedGuestCount() {
		return String.valueOf(mActivity.getGuestCount());
	}
}