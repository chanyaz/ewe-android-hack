package com.expedia.bookings.data.trips;

import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;

import com.expedia.bookings.data.Activity;
import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.Traveler;

public class ItinCardDataActivity extends ItinCardData {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	// SimpleDateFormat equiv: MMM d; eg Mar 15
	private static final int DETAIL_SHORT_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR
			| DateUtils.FORMAT_ABBREV_MONTH;
	// SimpleDateFormat equiv: MMMM d; eg March 15
	private static final int DETAIL_LONG_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
	// SimpleDateFormat equiv: EEEE, MMMM d, yyyy; eg Friday, March 15, 2012
	private static final int SHARE_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
			| DateUtils.FORMAT_SHOW_WEEKDAY;

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

	public DateTime getValidDate() {
		return getTripComponent().getParentTrip().getStartDate();
	}

	public DateTime getExpirationDate() {
		return getTripComponent().getParentTrip().getEndDate();
	}

	public String getFormattedShareValidDate(Context context) {
		return getValidDate().formatTime(context, SHARE_DATE_FLAGS);
	}

	public String getFormattedShareExpiresDate(Context context) {
		return getExpirationDate().formatTime(context, SHARE_DATE_FLAGS);
	}

	public String getLongFormattedValidDate(Context context) {
		return getValidDate().formatTime(context, DETAIL_LONG_DATE_FLAGS);
	}

	public String getFormattedValidDate(Context context) {
		return getValidDate().formatTime(context, DETAIL_SHORT_DATE_FLAGS);
	}

	public String getFormattedExpirationDate(Context context) {
		return getExpirationDate().formatTime(context, DETAIL_SHORT_DATE_FLAGS);
	}

	public int getGuestCount() {
		return mActivity.getGuestCount();
	}

	public String getFormattedGuestCount() {
		return String.valueOf(getGuestCount());
	}

	public List<Traveler> getTravelers() {
		return mActivity.getTravelers();
	}

	public String getVoucherPrintUrl() {
		return mActivity.getVoucherPrintUrl();
	}
}
