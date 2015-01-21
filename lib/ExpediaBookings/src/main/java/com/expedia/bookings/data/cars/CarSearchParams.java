package com.expedia.bookings.data.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.utils.DateUtils;

public class CarSearchParams {

	public DateTime startDateTime;
	public DateTime endDateTime;

	public String origin;

	@Override
	public CarSearchParams clone() {
		CarSearchParams clone = new CarSearchParams();
		clone.startDateTime = new DateTime(startDateTime);
		clone.endDateTime = new DateTime(endDateTime);
		clone.origin = origin;
		return clone;
	}

	public String toServerPickupDate() {
		return DateUtils.carSearchFormatFromDateTime(startDateTime);
	}

	public String toServerDropOffDate() {
		return DateUtils.carSearchFormatFromDateTime(endDateTime);
	}
}
