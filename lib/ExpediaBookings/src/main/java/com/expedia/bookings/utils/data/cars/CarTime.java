package com.expedia.bookings.utils.data.cars;

/*
 * A convenience class to store both epochseconds and timeZoneOffsetSeconds, plus more.
 * Can be used for pickupTime and dropOffTime.
 */
public class CarTime {

	public String raw;
	public String localized;
	public String epochSeconds;
	public String timeZoneOffsetSeconds;
	public String localizedShortDate;
}
