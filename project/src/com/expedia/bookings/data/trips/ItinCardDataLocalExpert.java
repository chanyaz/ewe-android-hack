package com.expedia.bookings.data.trips;

import android.content.Context;
import android.content.Intent;

public class ItinCardDataLocalExpert extends ItinCardData {
	public ItinCardDataLocalExpert(TripComponent tripComponent) {
		super(tripComponent);
	}

	@Override
	public boolean hasDetailData() {
		return false;
	}
}
