package com.expedia.bookings.data.cars;

import org.joda.time.DateTime;

// This is used as an optimization so on search we don't parse these fields since we don't use them
public class CreateTripCarOffer extends BaseCarOffer {
	private DateTime pickupTime;
	private DateTime dropOffTime;
	public CreateTripCarFare detailedFare;
	public final String rulesAndRestrictionsURL;

	public DateTime getPickupTime() {
		return pickupTime.toLocalDateTime().toDateTime();
	}

	public DateTime getDropOffTime() {
		return dropOffTime.toLocalDateTime().toDateTime();
	}

	public void setPickupTime(DateTime pickupTime) {
		this.pickupTime = pickupTime;
	}

	public void setDropOffTime(DateTime dropOffTime) {
		this.dropOffTime = dropOffTime;
	}
}
