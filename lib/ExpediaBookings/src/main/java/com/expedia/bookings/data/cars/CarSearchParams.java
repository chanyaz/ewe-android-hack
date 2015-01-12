package com.expedia.bookings.data.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.utils.DateUtils;

public class CarSearchParams {

    public DateTime startTime;
    public DateTime endTime;

    public String origin;

	public String toServerPickupDate() {
		return DateUtils.carSearchFormatFromDateTime(startTime);
	}

	public String toServerDropOffDate() {
		return DateUtils.carSearchFormatFromDateTime(endTime);
	}
}
