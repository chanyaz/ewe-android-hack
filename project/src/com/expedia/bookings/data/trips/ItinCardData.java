package com.expedia.bookings.data.trips;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.expedia.bookings.data.DateTime;
import com.google.android.gms.maps.model.LatLng;
import com.mobiata.android.Log;

/**
 * This class represents one item in the Itineraries list
 * Multiple ItinCardData objects may share the same tripComponent
 */
public class ItinCardData {

	private TripComponent mTripComponent;

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

	public TripComponent getTripComponent() {
		return mTripComponent;
	}

	public TripComponent.Type getTripComponentType() {
		return mTripComponent.getType();
	}

	public DateTime getStartDate() {
		return mTripComponent.getStartDate();
	}

	private static DateFormat sFormatter = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

	public int getStartDateSerialized() {
		DateTime date = getStartDate();
		if (date == null) {
			Log.e("getStartDate is null. Unable to sort.");
			return 0;
		}
		return Integer.parseInt(sFormatter.format(date.getMillisFromEpoch()));
	}

	public DateTime getEndDate() {
		return mTripComponent.getEndDate();
	}

	public String getDetailsUrl() {
		return mTripComponent.getParentTrip().getDetailsUrl();
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

	//////////////////////////////////////////////////////////////////////////
	// interfaces

	public interface ConfirmationNumberable {
		public boolean hasConfirmationNumber();

		public int getConfirmationNumberLabelResId();
		
		public String getFormattedConfirmationNumbers();
	}
}
