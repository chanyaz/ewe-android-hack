package com.expedia.bookings.data.trips;

import com.expedia.bookings.data.trips.TripComponent.Type;

public class ItinCardDataFallback extends ItinCardData {

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCardDataFallback(TripComponent tripComponent) {
		super(tripComponent);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public TripComponent.Type getTripComponentType() {
		return TripComponent.Type.FALLBACK;
	}

	public Type getType() {
		return getTripComponent().getType();
	}

	@Override
	public boolean hasSummaryData() {
		return false;
	}

	@Override
	public boolean hasDetailData() {
		return false;
	}

}