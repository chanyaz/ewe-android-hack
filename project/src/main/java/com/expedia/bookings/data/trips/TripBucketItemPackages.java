package com.expedia.bookings.data.trips;

import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse;

public class TripBucketItemPackages extends TripBucketItem {

	public MultiItemApiCreateTripResponse multiItemApiCreateTripResponse;

	public TripBucketItemPackages(MultiItemApiCreateTripResponse packageTripResponse) {
		multiItemApiCreateTripResponse = packageTripResponse;
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.PACKAGES;
	}
}
