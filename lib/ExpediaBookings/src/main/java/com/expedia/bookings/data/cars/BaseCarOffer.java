package com.expedia.bookings.data.cars;

import com.google.gson.annotations.SerializedName;

public class BaseCarOffer {
	public final String productKey;
	public CarVendor vendor;
	@SerializedName("creditCardRequiredToGuaranteeReservation")
	public final boolean checkoutRequiresCard;
	public CarLocation pickUpLocation;
	public final CarLocation dropOffLocation;
	public CarInfo vehicleInfo;
	public boolean hasFreeCancellation;
	public boolean hasUnlimitedMileage;
	public final boolean isInsuranceIncluded;
}
