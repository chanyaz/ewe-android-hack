package com.expedia.bookings.data.hotels;

import com.expedia.bookings.data.cars.Suggestion;
import com.google.gson.annotations.SerializedName;

public class SuggestionV4 {

	public String gaiaId;
	public String type;
	public RegionNames regionNames;
	public HierarchyInfo hierarchyInfo;
	public String hotelId;

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

	public static SuggestionV4 convertV1toV4(Suggestion suggest) {
		SuggestionV4 v4 = new SuggestionV4();
		v4.gaiaId = suggest.gaiaId;
		v4.type = suggest.type;
		HierarchyInfo info = new HierarchyInfo();
		info.isChild = false;
		v4.hierarchyInfo = info;
		RegionNames names = new RegionNames();
		names.displayName = suggest.displayName;
		names.shortName = suggest.shortName;
		names.fullName = suggest.fullName;
		v4.regionNames = names;
		LatLng latLng = new LatLng();
		latLng.lat = suggest.latLong.lat;
		latLng.lng = suggest.latLong.lng;
		v4.coordinates = latLng;
		v4.iconType = IconType.CURRENT_LOCATION_ICON;
		return v4;
	}
}
