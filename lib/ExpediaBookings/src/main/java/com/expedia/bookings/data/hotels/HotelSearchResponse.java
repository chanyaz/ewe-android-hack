package com.expedia.bookings.data.hotels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotelSearchResponse {
	public List<Hotel> hotelList;
	public List<Neighborhood> allNeighborhoodsInSearchRegion;

	public transient Map<String, Neighborhood> neighborhoodsMap = new HashMap<>();

	public static class Neighborhood {
		public String name;
		public String id;

		public transient List<Hotel> hotels = new ArrayList<>();
		public transient int score;
	}
}
