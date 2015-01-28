package com.expedia.bookings.data.cars;

import org.joda.time.DateTime;

public class CarOffer {
	public String productKey;
	public CarVendor vendor;
	public boolean reservationRequired;
	public CarLocation pickUpLocation;
	public CarLocation dropOffLocation;
	public DateTime pickupTime;
	public DateTime dropOffTime;
	public CarFare fare;
	public CarInfo vehicleInfo;
}
