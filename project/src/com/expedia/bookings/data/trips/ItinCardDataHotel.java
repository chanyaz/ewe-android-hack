package com.expedia.bookings.data.trips;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;

public class ItinCardDataHotel extends ItinCardData {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final SimpleDateFormat DETAIL_DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.getDefault());

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
		else if (mProperty.getThumbnail().getUrl() != null) {
			return mProperty.getThumbnail().getUrl();
		}

		return null;
	}

	public String getHeaderText() {
		if (mProperty != null) {
			return mProperty.getName();
		}

		return null;
	}

	public String getPropertyName() {
		if (mProperty != null) {
			return mProperty.getName();
		}

		return null;
	}

	public float getHotelRating() {
		if (mProperty != null) {
			return (float) mProperty.getHotelRating();
		}

		return 0;
	}

	public String getCheckInTime() {
		return ((TripHotel) getTripComponent()).getCheckInTime();
	}

	public String getFormattedCheckInDate() {
		return DETAIL_DATE_FORMAT.format(getStartDate().getCalendar().getTime());
	}

	public String getFormattedCheckOutDate() {
		return DETAIL_DATE_FORMAT.format(getEndDate().getCalendar().getTime());
	}

	public String getFormattedGuests() {
		return String.valueOf(((TripHotel) getTripComponent()).getGuests());
	}

	public Location getPropertyLocation() {
		if (mProperty != null) {
			return mProperty.getLocation();
		}

		return null;
	}

	public String getAddressString() {
		if (mProperty != null) {
			return mProperty.getLocation().getStreetAddressString();
		}

		return null;
	}

	public String getRelevantPhone() {
		if (mProperty == null) {
			return null;
		}

		if (!TextUtils.isEmpty(mProperty.getTollFreePhone())) {
			return mProperty.getTollFreePhone();
		}

		return mProperty.getLocalPhone();
	}

	public String getRoomDescription() {
		if (mProperty != null) {
			return mProperty.getDescriptionText();
		}

		return null;
	}

	public Intent getDirectionsIntent() {
		if (mProperty == null) {
			return null;
		}

		final String address = mProperty.getLocation().getStreetAddressString();
		final Uri uri = Uri.parse("http://maps.google.com/maps?daddr=" + address);

		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setComponent(new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"));

		return intent;
	}
}