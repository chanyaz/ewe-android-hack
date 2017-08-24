package com.expedia.bookings.data.rail.responses;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import com.expedia.bookings.data.rail.RailTravelMedium;
import com.expedia.bookings.utils.DateUtils;

public class RailSegment {
	public static final String TRANSFER = "Transfer";

	public Integer travelSegmentIndex;
	public final String travelMode;
	public RailStation departureStation;
	public RailStation arrivalStation;
	public final RailDateTime departureDateTime;
	public final RailDateTime arrivalDateTime;
	public String marketingCarrier; //"Virgin"
	public String operatingCarrier; //"Virgin"
	public final RailTravelMedium travelMedium;
	public final String duration;  //ISO duration format P[yY][mM][dD][T[hH][mM][s[.s]S]]

	public int durationMinutes() {
		return DateUtils.parseDurationMinutesFromISOFormat(duration);
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
