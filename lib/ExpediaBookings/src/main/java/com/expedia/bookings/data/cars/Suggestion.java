package com.expedia.bookings.data.cars;

import java.util.Locale;

import com.expedia.bookings.utils.Strings;
import com.google.gson.annotations.SerializedName;

public class Suggestion implements Cloneable {
	@SerializedName("f")
	public String fullName;

	@SerializedName("a")
	public String airportCode;

	@SerializedName("d")
	public String displayName;

	public String id;

	public String gaiaId;

	@SerializedName("t")
	public String type;

	@SerializedName("rt")
	public String regionType;

	@SerializedName("s")
	public String shortName;

	@SerializedName("l")
	public String longName;

	@SerializedName("c")
	public String countryName;

	@SerializedName("ll")
	public LatLong latLong;

	public boolean isMinorAirport;

	public IconType iconType = IconType.SEARCH_TYPE_ICON;

	public static enum IconType {
		HISTORY_ICON,
		CURRENT_LOCATION_ICON,
		SEARCH_TYPE_ICON
	}

	@Override
	public Suggestion clone() {
		try {
			return (Suggestion) super.clone();
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public boolean isMajorAirport() {
		return Strings.isNotEmpty(regionType) && regionType.toLowerCase(Locale.US).equals("airport") && !isMinorAirport;
	}
}
