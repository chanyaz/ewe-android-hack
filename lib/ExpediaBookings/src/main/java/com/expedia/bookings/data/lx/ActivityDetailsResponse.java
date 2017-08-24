package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.data.cars.LatLong;
import com.google.gson.annotations.SerializedName;

public class ActivityDetailsResponse {
	public final String id;
	public String title;
	public final String description;
	public final List<ActivityImages> images;
	public String fromPrice;
	public LXTicketType fromPriceTicketCode;
	public final String location;
	public final List<String> highlights;
	public final OffersDetail offersDetail;
	public String currencyCode;
	public final String regionId;
	public final List<String> inclusions;
	public final List<String> exclusions;
	public final List<String> knowBeforeYouBook;
	public final int freeCancellationMinHours;
	@SerializedName("typeGT")
	public boolean isGroundTransport;
	public String passengers;
	public String bags;
	public final String destination;
	public LXRedemptionType redemptionType;
	public final int recommendationScore;
	public final LXLocation eventLocation;
	public final List<LXLocation> redemptionLocation = new ArrayList<>();

	public static class LXLocation {
		public String addressName;
		public String street;
		public String city;
		public String province;
		public String postalCode;
		public final String latLng;

		public static LatLong getLocation(String latLong) {
			String[] latLongArray = latLong.split(",");
			LatLong location = LatLong.fromLatLngStrings(latLongArray[0], latLongArray[1]);
			return location;
		}
	}
}
