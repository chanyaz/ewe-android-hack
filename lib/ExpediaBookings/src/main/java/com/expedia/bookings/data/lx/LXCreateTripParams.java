package com.expedia.bookings.data.lx;

import java.util.List;

public class LXCreateTripParams {

	public String tripName;
	public List<LXOfferSelected> items;

	public LXCreateTripParams tripName(String tripName) {
		this.tripName = tripName;
		return this;
	}

	public LXCreateTripParams offersSelected(List<LXOfferSelected> offersSelected) {
		this.items = offersSelected;
		return this;
	}
}

