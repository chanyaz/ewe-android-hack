package com.mobiata.flightlib.data;

import org.joda.time.DateTime;

import com.mobiata.flightlib.utils.DateTimeUtils;

public class Layover {

	public final int mDuration;

	/**
	 * Automatically constructs a Layover between two flights.
	 * Flights must actually end and start in the same airport.
	 * @param flight1 the first flight, arrives in target airport
	 * @param flight2 the second flight, departs from target airport
	 */
	public Layover(Flight flight1, Flight flight2) {
		DateTime arrival = flight1.getArrivalWaypoint().getMostRelevantDateTime();
		DateTime departure = flight2.getOriginWaypoint().getMostRelevantDateTime();
		mDuration = DateTimeUtils.getMinutesBetween(arrival, departure);
	}

}
