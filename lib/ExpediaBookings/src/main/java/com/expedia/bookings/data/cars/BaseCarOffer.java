package com.expedia.bookings.data.cars;

import com.google.gson.annotations.SerializedName;

public class BaseCarOffer {
	public String productKey;
	public CarVendor vendor;
	@SerializedName("creditCardRequiredToGuaranteeReservation")
	public boolean checkoutRequiresCard;
	public CarLocation pickUpLocation;
	public CarLocation dropOffLocation;
	public CarInfo vehicleInfo;
	public boolean hasFreeCancellation;
	public boolean hasUnlimitedMileage;
	public boolean isInsuranceIncluded;
}
