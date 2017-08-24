package com.expedia.bookings.data.hotels;

import java.util.List;

public class RoomInfoFields {

	final String room;

	public RoomInfoFields(int numberOfAdults, List<Integer> childAges) {
		StringBuilder sb = new StringBuilder();
		sb.append(numberOfAdults);
		for (int childAge : childAges) {
			sb.append(",");
			sb.append(childAge);
		}
		room = sb.toString();
	}
}
