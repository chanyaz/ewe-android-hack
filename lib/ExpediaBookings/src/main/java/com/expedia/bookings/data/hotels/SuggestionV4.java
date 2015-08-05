package com.expedia.bookings.data.hotels;

import com.google.gson.annotations.SerializedName;

public class SuggestionV4 {

	public String gaiaId;
	public String type;
	public RegionNames regionNames;
	public HierarchyInfo hierarchyInfo;

	public LatLng coordinates;

	public IconType iconType = IconType.SEARCH_TYPE_ICON;

	public static class RegionNames {
		public String fullName;
		public String displayName;
		public String shortName;
	}

	public static class HierarchyInfo {
		public Airport airport;
		public Country country;
		public Boolean isChild = false;
	}

	public static class Airport {
		public String airportCode;
	}

	public static class Country {
		@SerializedName("name")
		public String countryName;
		@SerializedName("isoCode2")
		public String isoTwoLetterCode;
		@SerializedName("isoCode3")
		public String isoThreeLetterCode;
	}

	public static class LatLng {
		public double lat;
		@SerializedName("long")
		public double lng;
	}

	public static enum IconType {
		HISTORY_ICON,
		CURRENT_LOCATION_ICON,
		SEARCH_TYPE_ICON
	}

}
