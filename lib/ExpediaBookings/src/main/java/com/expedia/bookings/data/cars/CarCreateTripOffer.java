package com.expedia.bookings.data.cars;

import org.joda.time.DateTime;

// This is used as an optimization so on search we don't parse these fields since we don't use them
public class CarCreateTripOffer extends CarOffer {
	public DateTime pickupTime;
	public DateTime dropOffTime;
}
