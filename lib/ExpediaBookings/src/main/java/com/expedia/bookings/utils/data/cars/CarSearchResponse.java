package com.expedia.bookings.utils.data.cars;

import java.util.List;

import org.joda.time.DateTime;

public class CarSearchResponse {
	public DateTime pickupTime;
	public DateTime dropOffTime;
	public List<CarOffer> offers;
}
