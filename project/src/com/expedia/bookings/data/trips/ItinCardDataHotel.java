package com.expedia.bookings.data.trips;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.trips.ItinCardData.ConfirmationNumberable;

public class ItinCardDataHotel extends ItinCardData implements ConfirmationNumberable {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final Format DETAIL_DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.getDefault());
	private static final Format SHARE_CHECK_IN_FORMAT = new SimpleDateFormat("EEE MMM d", Locale.getDefault());
	private static final Format SHARE_CHECK_OUT_FORMAT = new SimpleDateFormat("EEE MMM d yyyy", Locale.getDefault());
	private static final Format LONG_SHARE_DATE_FORMAT = new SimpleDateFormat("EEEE MMMM d, yyyy", Locale.getDefault());

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
		if (mProperty.getMediaCount() > 0) {
			return mProperty.getMedia(0).getUrl(Media.IMAGE_BIG_SUFFIX);
		}

		return mProperty.getThumbnail().getUrl();
	}

	public String getPropertyName() {
		return mProperty.getName();
	}

	public float getPropertyRating() {
		return (float) mProperty.getHotelRating();
	}

	public String getPropertyInfoSiteUrl() {
		return mProperty.getInfoSiteUrl();
	}

	public String getFormattedLengthOfStay(Context context) {
		int nights = (int) ((getEndDate().getMillisFromEpoch() - getStartDate().getMillisFromEpoch()) / (1000 * 60 * 60 * 24));
		return context.getResources().getQuantityString(R.plurals.length_of_stay, nights, nights);
	}

	public String getCheckInTime() {
		return ((TripHotel) getTripComponent()).getCheckInTime();
	}

	public String getFormattedDetailsCheckInDate() {
		return DETAIL_DATE_FORMAT.format(getStartDate().getCalendar().getTime());
	}

	public String getFormattedDetailsCheckOutDate() {
		return DETAIL_DATE_FORMAT.format(getEndDate().getCalendar().getTime());
	}

	public String getFormattedShortShareCheckInDate() {
		return SHARE_CHECK_IN_FORMAT.format(getStartDate().getCalendar().getTime());
	}

	public String getFormattedShortShareCheckOutDate() {
		return SHARE_CHECK_OUT_FORMAT.format(getEndDate().getCalendar().getTime());
	}

	public String getFormattedLongShareCheckInDate() {
		return LONG_SHARE_DATE_FORMAT.format(getStartDate().getCalendar().getTime());
	}

	public String getFormattedLongShareCheckOutDate() {
		return LONG_SHARE_DATE_FORMAT.format(getEndDate().getCalendar().getTime());
	}

	public String getFormattedGuests() {
		return String.valueOf(((TripHotel) getTripComponent()).getGuests());
	}

	public Location getPropertyLocation() {
		return mProperty.getLocation();
	}

	public String getAddressString() {
		return mProperty.getLocation().getStreetAddressString();
	}

	public String getRelevantPhone() {
		if (!TextUtils.isEmpty(mProperty.getTollFreePhone())) {
			return mProperty.getTollFreePhone();
		}

		return mProperty.getLocalPhone();
	}

	public String getRoomDescription() {
		return mProperty.getDescriptionText();
	}

	public Intent getDirectionsIntent() {
		final String address = mProperty.getLocation().getStreetAddressString();
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
}