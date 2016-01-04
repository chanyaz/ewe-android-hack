package com.expedia.bookings.data.hotels;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.utils.Strings;
import com.google.gson.annotations.SerializedName;

public class SuggestionV4 {

	public String gaiaId;
	public String type;
	public RegionNames regionNames;
	@Nullable
	public HierarchyInfo hierarchyInfo;
	public String hotelId;

	public LatLng coordinates;

	public IconType iconType = IconType.SEARCH_TYPE_ICON;
	public boolean isSearchThisArea;

	public static class RegionNames {
		public String fullName;
		public String displayName;
		public String shortName;
	}

	public static class HierarchyInfo {
		@Nullable
		public Airport airport;
		public boolean isChild = false;
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
		SEARCH_TYPE_ICON,
		MAGNIFYING_GLASS_ICON
	}

	public SuggestionV4 copy() {
		SuggestionV4 v4 = new SuggestionV4();
		v4.gaiaId = gaiaId;
		v4.type = type;
		v4.hierarchyInfo = new HierarchyInfo();
		if (hierarchyInfo != null) {
			v4.hierarchyInfo.isChild = hierarchyInfo.isChild;
			v4.hierarchyInfo.airport = new Airport();
			if (hierarchyInfo.airport != null) {
				v4.hierarchyInfo.airport.airportCode = hierarchyInfo.airport.airportCode;
			}
		}
		v4.regionNames = new RegionNames();
		v4.regionNames.fullName = regionNames.fullName;
		v4.regionNames.displayName = regionNames.displayName;
		v4.regionNames.shortName = regionNames.shortName;
		v4.hotelId = hotelId;
		v4.coordinates = new LatLng();
		v4.coordinates.lat = coordinates.lat;
		v4.coordinates.lng = coordinates.lng;
		v4.iconType = iconType;
		return v4;
	}

	public boolean isCurrentLocationSearch() {
		return Strings.isEmpty(gaiaId) && !isSearchThisArea;
	}

	public boolean isGoogleSuggestionSearch() {
		return "GOOGLE_SUGGESTION_SEARCH".equals(type);
	}
}
