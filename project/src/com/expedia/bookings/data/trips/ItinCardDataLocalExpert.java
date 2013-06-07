package com.expedia.bookings.data.trips;

public class ItinCardDataLocalExpert extends ItinCardData {
	public ItinCardDataLocalExpert(TripComponent tripComponent) {
		super(tripComponent);
	}

	@Override
	public boolean hasDetailData() {
		return false;
	}
}
