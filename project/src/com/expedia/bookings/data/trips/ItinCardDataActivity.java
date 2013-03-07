package com.expedia.bookings.data.trips;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.expedia.bookings.data.Activity;
import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.Traveler;

public class ItinCardDataActivity extends ItinCardData {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final Format DETAIL_SHORT_DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.getDefault());
	private static final Format DETAIL_LONG_DATE_FORMAT = new SimpleDateFormat("MMMM d", Locale.getDefault());
	private static final Format SHARE_DATE_FORMAT = new SimpleDateFormat("EEEE MMMM d, yyyy", Locale.getDefault());

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

	public String getFormattedShareValidDate() {
		if (hasContent()) {
			return SHARE_DATE_FORMAT.format(getValidDate().getCalendar().getTime());
		}
		return null;
	}

	public String getFormattedShareExpiresDate() {
		if (hasContent()) {
			return SHARE_DATE_FORMAT.format(getValidDate().getCalendar().getTime());
		}
		return null;
	}

	public String getLongFormattedValidDate() {
		if (hasContent()) {
			return DETAIL_LONG_DATE_FORMAT.format(getValidDate().getCalendar().getTime());
		}
		return null;
	}

	public String getFormattedValidDate() {
		if (hasContent()) {
			return DETAIL_SHORT_DATE_FORMAT.format(getValidDate().getCalendar().getTime());
		}
		return null;
	}

	public String getFormattedExpirationDate() {
		if (hasContent()) {
			return DETAIL_SHORT_DATE_FORMAT.format(getExpirationDate().getCalendar().getTime());
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