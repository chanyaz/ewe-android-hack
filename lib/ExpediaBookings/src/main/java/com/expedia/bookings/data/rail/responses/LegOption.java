package com.expedia.bookings.data.rail.responses;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import com.expedia.bookings.data.Money;

public abstract class LegOption {
	public Integer legOptionIndex;
	public RailStation departureStation;
	public RailStation arrivalStation;
	public RailDateTime departureDateTime;
	public RailDateTime arrivalDateTime;
	public String aggregatedMarketingCarrier;
	public String aggregatedOperatingCarrier;
	public Money bestPrice;

	//TODO These are workarounds for now until API team makes search and create trip responses consistent
	public abstract int durationMinutes();

	public abstract List<? extends RailSegment> getTravelSegments();

	public String allOperators() {
		boolean first = true;
		String result = "";
		for (RailSegment segment : getTravelSegments()) {
			if (!first) {
				result += ", ";
			}
			result += segment.marketingCarrier;
			first = false;
		}

		return result;
	}

	@NotNull
	public DateTime getDepartureDateTime() {
		return departureDateTime.toDateTime();
	}

	@NotNull
	public DateTime getArrivalDateTime() {
		return arrivalDateTime.toDateTime();
	}

	public int changesCount() {
		return getTravelSegments().size() - 1;
	}
}
