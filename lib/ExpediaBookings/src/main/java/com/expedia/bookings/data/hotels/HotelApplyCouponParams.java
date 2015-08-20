package com.expedia.bookings.data.hotels;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class HotelApplyCouponParams {
	public String tripId;
	public String couponCode;

	public HotelApplyCouponParams(String tripId, String couponCode) {
		this.tripId = tripId;
		this.couponCode = couponCode;
	}

	@NotNull
	public Map<String, Object> toQueryMap() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tripId", tripId);
		params.put("coupon.code", couponCode);

		return params;
	}
}
