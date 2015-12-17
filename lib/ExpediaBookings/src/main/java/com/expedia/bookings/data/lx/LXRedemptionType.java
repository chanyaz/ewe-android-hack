package com.expedia.bookings.data.lx;

import com.google.gson.annotations.SerializedName;

public enum LXRedemptionType {
	@SerializedName("Print")
	PRINT,
	@SerializedName("Voucherless")
	VOUCHERLESS
}
