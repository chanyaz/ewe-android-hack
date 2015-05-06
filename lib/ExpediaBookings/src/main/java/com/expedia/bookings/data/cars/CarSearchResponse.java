package com.expedia.bookings.data.cars;

import java.util.List;

import org.joda.time.DateTime;

public class CarSearchResponse extends BaseApiResponse {
	public DateTime pickupTime;
	public DateTime dropOffTime;
	public List<SearchCarOffer> offers;
}
