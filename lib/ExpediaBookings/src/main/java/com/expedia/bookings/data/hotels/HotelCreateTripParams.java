package com.expedia.bookings.data.hotels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class HotelCreateTripParams {
	final String productKey;
	final boolean qualifyAirAttach;
	final RoomInfoFields roomInfoFields;

	public HotelCreateTripParams(String productKey, boolean qualifyAirAttach, int numberOfAdults, List<Integer> childAges) {
		this.productKey = productKey;
		this.qualifyAirAttach = qualifyAirAttach;
		this.roomInfoFields = new RoomInfoFields(numberOfAdults, childAges);
	}

	@NotNull
	public Map<String, Object> toQueryMap() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("productKey", productKey);
		params.put("qualifyAirAttach", qualifyAirAttach);
		params.put("roomInfoFields[0].room", roomInfoFields.room);

		return params;
	}
}
