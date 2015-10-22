package com.expedia.bookings.data.hotels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.expedia.bookings.data.cars.BaseApiResponse;

public class HotelSearchResponse extends BaseApiResponse {
	public String pageViewBeaconPixelUrl;
	public List<Hotel> hotelList;
	public List<Neighborhood> allNeighborhoodsInSearchRegion;
	public Map<String, AmenityOptions> amenityFilterOptions;
	public String searchRegionId;
	public List<PriceOption> priceOptions;

	public transient Map<String, Neighborhood> neighborhoodsMap = new HashMap<>();
	public transient HotelRate.UserPriceType userPriceType = HotelRate.UserPriceType.UNKNOWN;
	public transient boolean isFilteredResponse;

	public static class Neighborhood {
		public String name;
		public String id;

		public transient List<Hotel> hotels = new ArrayList<>();
		public transient int score;
	}

	public static class AmenityOptions {
	}

	public static class PriceOption {
		public Integer minPrice;
		public Integer maxPrice;
	}
}
