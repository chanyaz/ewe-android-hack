package com.expedia.bookings.data.trips;

import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.trips.ItinCardData.ConfirmationNumberable;
import com.google.android.gms.maps.model.LatLng;

public class ItinCardDataHotel extends ItinCardData implements ConfirmationNumberable {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final int DETAIL_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH;
	private static final int LONG_SHARE_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY;
	private static final int SHARE_CHECK_IN_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;
	private static final int SHARE_CHECK_OUT_FLAGS = LONG_SHARE_DATE_FLAGS | DateUtils.FORMAT_ABBREV_WEEKDAY;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Property mProperty;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCardDataHotel(TripHotel tripComponent) {
		super(tripComponent);
		mProperty = tripComponent.getProperty();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public String getHeaderImageUrl() {
		if (hasProperty()) {
			if (mProperty.getMediaCount() > 0) {
				return mProperty.getMedia(0).getUrl(Media.IMAGE_BIG_SUFFIX);
			}

			return mProperty.getThumbnail().getUrl();
		}
		return null;
	}

	public String getPropertyName() {
		if (hasProperty()) {
			return mProperty.getName();
		}
		else if (this.getTripComponent() != null && this.getTripComponent().getParentTrip() != null
				&& !TextUtils.isEmpty(this.getTripComponent().getParentTrip().getTitle())) {
			return this.getTripComponent().getParentTrip().getTitle();
		}
		else {
			return null;
		}
	}

	public float getPropertyRating() {
		if (hasProperty()) {
			return (float) mProperty.getHotelRating();
		}
		return 0f;
	}

	public String getPropertyInfoSiteUrl() {
		if (hasProperty()) {
			return mProperty.getInfoSiteUrl();
		}
		return null;
	}

	public String getFormattedLengthOfStay(Context context) {
		int nights = (int) ((getEndDate().getMillisFromEpoch() - getStartDate().getMillisFromEpoch()) / (1000 * 60 * 60 * 24));
		return context.getResources().getQuantityString(R.plurals.length_of_stay, nights, nights);
	}

	public String getCheckInTime() {
		return ((TripHotel) getTripComponent()).getCheckInTime();
	}

	public String getFormattedDetailsCheckInDate(Context context) {
		long startMillis = getStartDate().getCalendar().getTimeInMillis();
		return DateUtils.formatDateTime(context, startMillis, DETAIL_DATE_FLAGS);
	}

	public String getFormattedDetailsCheckOutDate(Context context) {
		long endMillis = getEndDate().getCalendar().getTimeInMillis();
		return DateUtils.formatDateTime(context, endMillis, DETAIL_DATE_FLAGS);
	}

	public String getFormattedShortShareCheckInDate(Context context) {
		long checkInMillis = getStartDate().getCalendar().getTimeInMillis();
		return DateUtils.formatDateTime(context, checkInMillis, SHARE_CHECK_IN_FLAGS);
	}

	public String getFormattedShortShareCheckOutDate(Context context) {
		long checkOutMillis = getEndDate().getCalendar().getTimeInMillis();
		return DateUtils.formatDateTime(context, checkOutMillis, SHARE_CHECK_OUT_FLAGS);
	}

	public String getFormattedLongShareCheckInDate(Context context) {
		long checkInMillis = getStartDate().getCalendar().getTimeInMillis();
		return DateUtils.formatDateTime(context, checkInMillis, LONG_SHARE_DATE_FLAGS);
	}

	public String getFormattedLongShareCheckOutDate(Context context) {
		long checkOutMillis = getEndDate().getCalendar().getTimeInMillis();
		return DateUtils.formatDateTime(context, checkOutMillis, LONG_SHARE_DATE_FLAGS);
	}

	public String getFormattedGuests() {
		return String.valueOf(((TripHotel) getTripComponent()).getGuests());
	}

	public Location getPropertyLocation() {
		if (hasProperty()) {
			return mProperty.getLocation();
		}
		return null;
	}

	public String getAddressString() {
		if (hasProperty()) {
			return mProperty.getLocation().getStreetAddressString();
		}
		return null;
	}

	public String getRelevantPhone() {
		if (hasProperty()) {
			if (!TextUtils.isEmpty(mProperty.getTollFreePhone())) {
				return mProperty.getTollFreePhone();
			}

			return mProperty.getLocalPhone();
		}
		return null;
	}

	public String getRoomDescription() {
		if (hasProperty()) {
			return mProperty.getDescriptionText();
		}
		return null;
	}

	public Intent getDirectionsIntent() {
		final String address = mProperty.getLocation().toLongFormattedString();
		final Uri uri = Uri.parse("http://maps.google.com/maps?daddr=" + address);

		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setComponent(new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"));

		return intent;
	}

	@Override
	public String getFormattedConfirmationNumbers() {
		Set<String> confirmationNumbers = ((TripHotel) getTripComponent()).getConfirmationNumbers();
		if (confirmationNumbers != null) {
			return TextUtils.join(",  ", confirmationNumbers.toArray());
		}

		return null;
	}

	@Override
	public boolean hasConfirmationNumber() {
		if (getTripComponent() != null && ((TripHotel) getTripComponent()).getConfirmationNumbers() != null
				&& ((TripHotel) getTripComponent()).getConfirmationNumbers().size() > 0) {
			return true;
		}
		return false;
	}

	@Override
	public int getConfirmationNumberLabelResId() {
		return R.string.hotel_confirmation_code_label;
	}

	private boolean hasProperty() {
		return mProperty != null;
	}

	@Override
	public LatLng getLocation() {
		Location loc = getPropertyLocation();

		if (loc != null) {
			return new LatLng(loc.getLatitude(), loc.getLongitude());
		}

		return super.getLocation();
	}
}
