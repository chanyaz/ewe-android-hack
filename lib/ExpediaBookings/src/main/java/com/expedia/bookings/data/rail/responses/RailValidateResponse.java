package com.expedia.bookings.data.rail.responses;

import com.google.gson.annotations.SerializedName;

public class RailValidateResponse {

	@SerializedName("railValidateOfferResult") // same structure as RailGetDetailsResult
	public RailGetDetailsResult railGetDetailsResult;
}
