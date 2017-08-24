package com.expedia.bookings.data.trips;

import org.joda.time.DateTime;

import com.google.android.gms.maps.model.LatLng;

/**
 * This class represents one item in the Itineraries list
 * Multiple ItinCardData objects may share the same tripComponent
 */
public class ItinCardData {

	private final TripComponent mTripComponent;

	// Non-persistant but unique ID across rotations etc.
	private String mId;

	public ItinCardData(TripComponent tripComponent) {
		mTripComponent = tripComponent;

		mId = tripComponent.getUniqueId();
	}

	public void setId(String id) {
		mId = id;
	}

	public String getId() {
		return mId;
	}

	public String getTripNumber() {
		return mTripComponent.getParentTrip().getTripNumber();
	}

	public String getTripId() {
		return mTripComponent.getParentTrip().getTripId();
	}

	public TripComponent getTripComponent() {
		return mTripComponent;
	}

	public TripComponent.Type getTripComponentType() {
		return mTripComponent.getType();
	}

	public DateTime getStartDate() {
		return mTripComponent.getStartDate();
	}

	public DateTime getEndDate() {
		return mTripComponent.getEndDate();
	}

	public String getDetailsUrl() {
		return mTripComponent.getParentTrip().getDetailsUrl();
	}

	public String getSharableDetailsUrl() {
		if (mTripComponent.getShareInfo().hasSharableUrl()) {
			return mTripComponent.getShareInfo().getSharableUrl();
		}
		return mTripComponent.getParentTrip().getShareInfo().getSharableUrl();
	}

	public boolean hasSummaryData() {
		return true;
	}

	public boolean hasDetailData() {
		return true;
	}

	/**
	 * Gets the location that this card represents.  Can change
	 * depending on time of day.
	 *
	 * @return null by default
	 */
	public LatLng getLocation() {
		return null;
	}

	public boolean isSharedItin() {
		return mTripComponent.getParentTrip().isShared();
	}

	//////////////////////////////////////////////////////////////////////////
	// interfaces

	public interface ConfirmationNumberable {
		boolean hasConfirmationNumber();

		int getConfirmationNumberLabelResId();

		String getFormattedConfirmationNumbers();
	}
}
