package com.expedia.bookings.data.lx;

import java.util.List;

public class LXCreateTripParams {
	public String tripName;
	public List<LXOfferSelected> offersSelected;

	public LXCreateTripParams tripName(String tripName) {
		this.tripName = tripName;
		return this;
	}

	public LXCreateTripParams offersSelected(List<LXOfferSelected> offersSelected) {
		this.offersSelected = offersSelected;
		return this;
	}
}
