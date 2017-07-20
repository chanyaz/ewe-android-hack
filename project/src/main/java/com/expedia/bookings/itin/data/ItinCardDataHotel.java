package com.expedia.bookings.itin.data;

import java.util.List;
import java.util.Set;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardData.ConfirmationNumberable;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.utils.JodaUtils;
import com.google.android.gms.maps.model.LatLng;

public class ItinCardDataHotel extends ItinCardData implements ConfirmationNumberable {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final int DETAIL_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR
			| DateUtils.FORMAT_ABBREV_MONTH;
	private static final int LONG_SHARE_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
			| DateUtils.FORMAT_SHOW_WEEKDAY;
	private static final int SHARE_CHECK_IN_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
			| DateUtils.FORMAT_ABBREV_WEEKDAY;
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

	public boolean hasFetchedUpgradeOffers() {
		return mProperty.getRoomUpgradeOfferType() != Property.RoomUpgradeType.NOT_CALLED_UPGRADE_API;
	}

	public boolean hasRoomUpgradeOffers() {
		return mProperty.getRoomUpgradeOfferType() == Property.RoomUpgradeType.HAS_UPGRADE_OFFERS;
	}

	public List<String> getHeaderImageUrls() {
		if (hasProperty()) {
			HotelMedia hotelMedia;
			if (mProperty.getMediaCount() > 0) {
				hotelMedia = mProperty.getMedia(0);
			}
			else {
				hotelMedia = mProperty.getThumbnail();
			}

			if (hotelMedia != null) {
				return hotelMedia.getHighResUrls();
			}
		}
		return null;
	}

	public Property getProperty() {
		return mProperty;
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

	public boolean isPastCheckInDate() {
		return getStartDate().isBeforeNow();
	}

	public String getCheckInTime() {
		return ((TripHotel) getTripComponent()).getCheckInTime();
	}

	public String getFormattedDetailsCheckInDate(Context context) {
		return JodaUtils.formatDateTime(context, getStartDate(), DETAIL_DATE_FLAGS);
	}

	public String getCheckOutTime() {
		return ((TripHotel) getTripComponent()).getCheckOutTime();
	}

	public String getFormattedDetailsCheckOutDate(Context context) {
		return JodaUtils.formatDateTime(context, getEndDate(), DETAIL_DATE_FLAGS);
	}

	public String getFallbackCheckInTime(Context context) {
		return TextUtils.isEmpty(getCheckInTime())
				? JodaUtils.formatDateTime(context, getStartDate(), DateUtils.FORMAT_SHOW_TIME)
				: getCheckInTime();
	}

	public String getFallbackCheckOutTime(Context context) {
		return TextUtils.isEmpty(getCheckOutTime())
				? JodaUtils.formatDateTime(context, getEndDate(), DateUtils.FORMAT_SHOW_TIME)
				: getCheckOutTime();
	}

	public int getGuestCount() {
		return ((TripHotel) getTripComponent()).getGuests();
	}

	public String getFormattedGuests() {
		return String.valueOf(getGuestCount());
	}

	public Location getPropertyLocation() {
		if (hasProperty()) {
			return mProperty.getLocation();
		}
		return null;
	}

	public String getAddressString() {
		if (hasProperty()) {
			return mProperty.getLocation().toLongFormattedString();
		}
		return null;
	}

	public String getPropertyCity() {
		if (hasProperty()) {
			return mProperty.getLocation().getCity();
		}
		return null;
	}

	public String getLocalPhone() {
		if (hasProperty()) {
			return mProperty.getLocalPhone();
		}
		return null;
	}

	public String getTollFreePhone() {
		if (hasProperty()) {
			return mProperty.getTollFreePhone();
		}
		return null;
	}

	public String getRelevantPhone() {
		if (hasProperty()) {
			return mProperty.getRelevantPhone();
		}
		return null;
	}

	public String getBedType() {
		if (hasProperty()) {
			return mProperty.getItinBedType();
		}
		return null;
	}

	public String getNonPricePromotionText() {
		if (hasProperty()) {
			return mProperty.getItinNonPricePromotionText();
		}
		return null;
	}

	public String getRoomType() {
		if (hasProperty()) {
			return mProperty.getItinRoomType();
		}
		return null;
	}

	public boolean isVip() {
		if (hasProperty()) {
			return mProperty.isVipAccess();
		}
		return false;
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

	public String getLastConfirmationNumber() {
		if (hasConfirmationNumber()) {
			Set<String> setOfConfirmationNumbers = ((TripHotel) getTripComponent()).getConfirmationNumbers();
			String[] confirmationNumbers = setOfConfirmationNumbers
				.toArray(new String[setOfConfirmationNumbers.size()]);
			if (confirmationNumbers.length > 0) {
				return confirmationNumbers[confirmationNumbers.length - 1];
			}
		}
		return null;
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
