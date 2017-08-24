package com.expedia.bookings.data.rail.responses;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.DateUtils;

public class RailLegOption implements Comparable<RailLegOption> {
	public final Integer legOptionIndex;
	public final RailStation departureStation;
	public final RailStation arrivalStation;
	public final RailDateTime departureDateTime;
	public final RailDateTime arrivalDateTime;
	public List<RailSegment> travelSegmentList;
	public final String duration;  //ISO duration format P[yY][mM][dD][T[hH][mM][s[.s]S]]
	public String aggregatedMarketingCarrier;
	public String aggregatedOperatingCarrier;
	public Integer noOfChanges;
	public Money bestPrice;
	public boolean overtakenJourney;
	// Not returned by api but set in code.
	public final boolean doesAnyOfferHasFareQualifier;

	public int durationMinutes() {
		return DateUtils.parseDurationMinutesFromISOFormat(duration);
	}

	@NotNull
	public DateTime getDepartureDateTime() {
		return departureDateTime.toDateTime();
	}

	@NotNull
	public DateTime getArrivalDateTime() {
		return arrivalDateTime.toDateTime();
	}

	@Override
	public int compareTo(RailLegOption another) {
		return this.departureDateTime.compareTo(another.departureDateTime);
	}
}
