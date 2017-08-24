package com.expedia.bookings.data.rail.responses;

import java.util.List;

public class PassengerSegmentFare {

	public String fareCode;
	public String carrierServiceClassDisplayName;
	public String fareDescription;
	public String carrierFareClassCategoryName;
	public String carrierFareClassDisplayName;
	public final Integer travelSegmentIndex;
	public List<Amenity> amenityList;

	public static class Amenity {
		public String displayName;
	}
}
