package com.expedia.bookings.data.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.utils.DateUtils;

public class CarSearchParams {

	public DateTime startDateTime;
	public DateTime endDateTime;

	// Car Search requires:
	// 1. Origin
	//     OR
	// 2. pickupLocation
	// Both are mutually exclusive.
	// Origin is used for just Airport Code based searches. Pickup Location based searches allow for non-airport searches too.
	// If both are present, the consume should honor only one of them!
	public String origin;
	public LatLong pickupLocationLatLng;

	//A descriptive name of the Search Location, to be displayed in the Toolbar
	public String originDescription;

	@Override
	public CarSearchParams clone() {
		CarSearchParams clone = new CarSearchParams();
		clone.startDateTime = new DateTime(startDateTime);
		clone.endDateTime = new DateTime(endDateTime);
		clone.originDescription = originDescription;

		clone.origin = origin;
		if (pickupLocationLatLng != null) {
			clone.pickupLocationLatLng = new LatLong(pickupLocationLatLng.lat, pickupLocationLatLng.lng);
		}

		return clone;
	}

	public String toServerPickupDate() {
		return DateUtils.carSearchFormatFromDateTime(startDateTime);
	}

	public String toServerDropOffDate() {
		return DateUtils.carSearchFormatFromDateTime(endDateTime);
	}

	public boolean shouldSearchByLocationLatLng() {
		return pickupLocationLatLng != null;
	}
}
