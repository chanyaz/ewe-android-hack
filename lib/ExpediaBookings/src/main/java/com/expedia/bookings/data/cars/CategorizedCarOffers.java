package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rkochhar on 1/15/15.
 */
public class CategorizedCarOffers {
	private List<CarOffer> offers;
	private CarOffer selectedOffer;

	public CategorizedCarOffers() {
		offers = new ArrayList<>();
	}

	public List<CarOffer> getOffers() {
		return offers;
	}

	public CarOffer getSelectedOffer() {
		return selectedOffer;
	}

	public void setSelectedOffer(CarOffer selectedOffer) {
		this.selectedOffer = selectedOffer;
	}
}
