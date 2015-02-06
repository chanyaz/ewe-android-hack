package com.expedia.bookings.data.cars;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
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

	public boolean isMinorAirport;

	public boolean isHistory = false;

}
