package com.expedia.bookings.data.lx;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ActivityDetailsResponse {
	public String id;
	public String title;
	public String description;
	public List<ActivityImages> images;
	public String fromPrice;
	public LXTicketType fromPriceTicketCode;
	public String location;
	public List<String> highlights;
	public OffersDetail offersDetail;
	public String currencyCode;
	public String regionId;
	public List<String> inclusions;
	public List<String> exclusions;
	public List<String> knowBeforeYouBook;
	public int freeCancellationMinHours;
	@SerializedName("typeGT")
	public boolean isGroundTransport;
	public String passengers;
	public String bags;
	public String destination;
	public LXRedemptionType redemptionType;
	public int recommendationScore;
	public LXLocation eventLocation;
	public List<LXLocation> redemptionLocation;

	public static class LXLocation {
		public String addressName;
		public String street;
		public String city;
		public String province;
		public String postalCode;
		public String latLng;
	}
}
