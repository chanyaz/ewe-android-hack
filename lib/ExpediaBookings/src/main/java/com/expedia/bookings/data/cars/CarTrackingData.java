package com.expedia.bookings.data.cars;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mohsharma on 3/25/15.
 */
public class CarTrackingData {

	@SerializedName("INVENTORY_TYPE")
	public String inventoryType;

	@SerializedName("SIPP_CODE")
	public final String sippCode;

}
