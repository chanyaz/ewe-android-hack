package com.expedia.bookings.test.tests.pageModels.tablet;

public class TripBucket {

	private static final TripBucket TRIP_BUCKET = new TripBucket();

	private TripBucket() {
		// We don't want people instantiating their own
	}

	public static TripBucket getInstance() {
		return TRIP_BUCKET;
	}
}
