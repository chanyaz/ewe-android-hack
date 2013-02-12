package com.expedia.bookings.data.trips;

import android.text.TextUtils;

import com.expedia.bookings.data.Car;

public class ItinCardDataCar extends ItinCardData {
	private Car mCar;

	public ItinCardDataCar(TripComponent tripComponent) {
		super(tripComponent);
		mCar = ((TripCar) tripComponent).getCar();
	}

	public String getFormattedCarType() {
		if (mCar == null) {
			return null;
		}

		if (mCar.getType() == null) {
			return null;
		}

		String[] words = mCar.getType().toString().replace("_", " ").split(" ");
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
		}

		return TextUtils.join(" ", words);
	}

	public String getVendorName() {
		return mCar.getVendor().getShortName();
	}
}