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
		if (hasContent()) {
			return mActivity.getTitle();
		}
		return null;
	}

	public DateTime getValidDate() {
		if (hasContent()) {
			return getTripComponent().getParentTrip().getStartDate();
		}
		return null;
	}

	public DateTime getExpirationDate() {
		if (hasContent()) {
			return getTripComponent().getParentTrip().getEndDate();
		}
		return null;
	}

	public String getFormattedShareValidDate(Context context) {
		if (hasContent()) {
			long validMillis = getValidDate().getCalendar().getTimeInMillis();
			return DateUtils.formatDateTime(context, validMillis, SHARE_DATE_FLAGS);
		}
		return null;
	}

	public String getFormattedShareExpiresDate(Context context) {
		if (hasContent()) {
			long expiresMillis = getExpirationDate().getCalendar().getTimeInMillis();
			return DateUtils.formatDateTime(context, expiresMillis, SHARE_DATE_FLAGS);
		}
		return null;
	}

	public String getLongFormattedValidDate(Context context) {
		if (hasContent()) {
			long validMillis = getValidDate().getCalendar().getTimeInMillis();
			return DateUtils.formatDateTime(context, validMillis, DETAIL_LONG_DATE_FLAGS);
		}
		return null;
	}

	public String getFormattedValidDate(Context context) {
		if (hasContent()) {
			long validMillis = getValidDate().getCalendar().getTimeInMillis();
			return DateUtils.formatDateTime(context, validMillis, DETAIL_SHORT_DATE_FLAGS);
		}
		return null;
	}

	public String getFormattedExpirationDate(Context context) {
		if (hasContent()) {
			long expiresMillis = getExpirationDate().getCalendar().getTimeInMillis();
			return DateUtils.formatDateTime(context, expiresMillis, DETAIL_SHORT_DATE_FLAGS);
		}
		return null;
	}

	public String getFormattedGuestCount() {
		if (hasContent()) {
			return String.valueOf(mActivity.getGuestCount());
		}
		return null;
	}

	public List<Traveler> getTravelers() {
		if (hasContent()) {
			return mActivity.getTravelers();
		}
		return null;
	}

	public String getVoucherPrintUrl() {
		if (hasContent()) {
			return mActivity.getVoucherPrintUrl();
		}
		return null;
	}

	private boolean hasContent() {
		return mActivity != null;
	}
}
