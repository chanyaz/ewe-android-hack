package com.expedia.bookings.data.rail.responses;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

public abstract class RailSegment {
	
	public static final String TRANSFER = "Transfer";

	public String travelMode;
	public RailStation departureStation;
	public RailStation arrivalStation;
	public RailDateTime departureDateTime;
	public RailDateTime arrivalDateTime;
	public String marketingCarrier; //"Virgin"
	public String operatingCarrier; //"Virgin"
	public RailTravelMedium travelMedium;
	public Integer travelSegmentIndex;

	public abstract int durationMinutes();

	public boolean isTransfer() {
		return TRANSFER.equals(travelMode);
	}

	public static class RailTravelMedium {
		public String travelMediumCode; //"ICY"
		public String travelMediumName; //"Inter-City"
	}

	@NotNull
	public DateTime getDepartureDateTime() {
		return departureDateTime.toDateTime();
	}

	@NotNull
	public DateTime getArrivalDateTime() {
		return arrivalDateTime.toDateTime();
	}
}
