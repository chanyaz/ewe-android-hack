package com.expedia.bookings.data.collections;

import com.google.gson.annotations.SerializedName;

public class CollectionLocation {
	public String title;
	public String subtitle;
	public String description;
	public String id;
	public String imageCode;
	public Location location;

	public static class Location {
		@SerializedName("a")
		public String airportCode;

		@SerializedName("d")
		public String displayName;

		public String id;

		@SerializedName("t")
		public String type;

		@SerializedName("s")
		public String shortName;

		@SerializedName("ll")
		public LatLng latLong;
	}

	public static class LatLng {
		public double lat;
		public double lng;
	}
}
