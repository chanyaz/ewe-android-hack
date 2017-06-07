package com.expedia.bookings.data;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.utils.Strings;
import com.google.gson.annotations.SerializedName;

public class SuggestionV4 {

	transient public static final String CURRENT_LOCATION_ID = "current_location";

	public String gaiaId;
	public String type;
	public RegionNames regionNames;
	@Nullable
	public HierarchyInfo hierarchyInfo;
	public String hotelId;

	public LatLng coordinates;

	public boolean isMinorAirport;

	public enum IconType {
		HISTORY_ICON,
		CURRENT_LOCATION_ICON,
		SEARCH_TYPE_ICON,
		MAGNIFYING_GLASS_ICON
	}

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
		public Country country;
		public Rails rails;
		public boolean isChild = false;
	}

	public static class Airport {
		public String airportCode;
		public String multicity;
	}

	public static class Country {
		public String name;
		@SerializedName("isoCode3")
		public String countryCode;
	}

	public static class Rails {
		public String stationCode;
	}

	public static class LatLng {
		public double lat;
		@SerializedName("long")
		public double lng;
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
				v4.hierarchyInfo.airport.multicity = hierarchyInfo.airport.multicity;
			}
			v4.hierarchyInfo.rails = new Rails();
			if (hierarchyInfo.rails != null) {
				v4.hierarchyInfo.rails.stationCode = hierarchyInfo.rails.stationCode;
			}
			if (hierarchyInfo.country != null) {
				v4.hierarchyInfo.country = new Country();
				v4.hierarchyInfo.country.name = hierarchyInfo.country.name;
			}
		}
		v4.regionNames = new RegionNames();
		if (regionNames != null) {
			v4.regionNames.fullName = regionNames.fullName;
			v4.regionNames.displayName = regionNames.displayName;
			v4.regionNames.shortName = regionNames.shortName;
		}
		v4.hotelId = hotelId;
		v4.coordinates = new LatLng();
		if (coordinates != null) { //Rail sends null coordinates for parent stations
			v4.coordinates.lat = coordinates.lat;
			v4.coordinates.lng = coordinates.lng;
		}
		v4.iconType = iconType;
		return v4;
	}

	public boolean isChild() {
		return hierarchyInfo != null && hierarchyInfo.isChild;
	}

	public boolean isHistoryItem() {
		return iconType == IconType.HISTORY_ICON;
	}

	public boolean isCurrentLocationSearch() {
		return Strings.isEmpty(gaiaId) && !isSearchThisArea && Strings.isEmpty(hotelId);
	}

	public boolean isMajorAirport() {
		return Strings.isNotEmpty(type) && type.toLowerCase(Locale.US).equals("airport") && !isMinorAirport;
	}

	public boolean isGoogleSuggestionSearch() {
		return "GOOGLE_SUGGESTION_SEARCH".equals(type);
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 31 * result + ((gaiaId == null) ? 0 : gaiaId.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SuggestionV4)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		SuggestionV4 other = (SuggestionV4) obj;
		return this.gaiaId != null && this.gaiaId.equals(other.gaiaId);
	}
}
