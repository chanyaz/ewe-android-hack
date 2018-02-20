package com.expedia.bookings.data.rail.responses;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import com.expedia.bookings.data.rail.RailTravelMedium;
import com.expedia.bookings.utils.ApiDateUtils;

public class RailSegment {
	public static final String TRANSFER = "Transfer";

	public Integer travelSegmentIndex;
	public String travelMode;
	public RailStation departureStation;
	public RailStation arrivalStation;
	public RailDateTime departureDateTime;
	public RailDateTime arrivalDateTime;
	public String marketingCarrier; //"Virgin"
	public String operatingCarrier; //"Virgin"
	public RailTravelMedium travelMedium;
	public String duration;  //ISO duration format P[yY][mM][dD][T[hH][mM][s[.s]S]]

	public int durationMinutes() {
		return ApiDateUtils.parseDurationMinutesFromISOFormat(duration);
	}

	public boolean isTransfer() {
		return TRANSFER.equals(travelMode) && !RailTravelMedium.METRO_CITY_TRANSIT.equals(travelMedium.getTravelMediumCode());
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
