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
		public Boolean isChild = false;
	}

	public static class Airport {
		public String airportCode;
	}

	public static class LatLng {
		public double lat;
		@SerializedName("long")
		public double lng;
	}

	public enum IconType {
		HISTORY_ICON,
		CURRENT_LOCATION_ICON,
		SEARCH_TYPE_ICON
	}

}
