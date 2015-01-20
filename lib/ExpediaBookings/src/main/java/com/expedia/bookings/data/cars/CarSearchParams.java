package com.expedia.bookings.data.cars;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.expedia.bookings.utils.DateUtils;

public class CarSearchParams {

	public DateTime startTime;
	public DateTime endTime;

	public String origin;

	@Override
	public CarSearchParams clone() {
		CarSearchParams clone = new CarSearchParams();
		clone.startTime = new DateTime(startTime);
		clone.endTime = new DateTime(endTime);
		clone.origin = origin;
		return clone;
	}

	public String toServerPickupDate() {
		return DateUtils.carSearchFormatFromDateTime(startTime);
	}

	public String toServerDropOffDate() {
		return DateUtils.carSearchFormatFromDateTime(endTime);
	}
}
