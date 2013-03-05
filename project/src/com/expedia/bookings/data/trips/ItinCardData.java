package com.expedia.bookings.data.trips;

import com.expedia.bookings.data.DateTime;

/**
 * This class represents one item in the Itineraries list
 * Multiple ItinCardData objects may share the same tripComponent
 */
public class ItinCardData {

	private TripComponent mTripComponent;

	public ItinCardData(TripComponent tripComponent) {
		mTripComponent = tripComponent;
	}

	public TripComponent getTripComponent() {
		return mTripComponent;
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
	

	//////////////////////////////////////////////////////////////////////////
	// interfaces

	public interface ConfirmationNumberable {
		public boolean hasConfirmationNumber();
		
		public String getFormattedConfirmationNumbers();
	}
}
