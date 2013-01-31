package com.expedia.bookings.data.trips;

import java.util.Calendar;

import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.FlightLeg;

public class ItinCardDataFlight extends ItinCardData {

	private int mLegNumber;

	public ItinCardDataFlight(TripFlight parent, int leg) {
		super(parent);

		mLegNumber = leg;
	}

	public int getLegNumber() {
		return mLegNumber;
	}

	public FlightLeg getFlightLeg() {
		return ((TripFlight) getTripComponent()).getFlightTrip().getLeg(mLegNumber);
	}

	@Override
	public DateTime getStartDate() {
		Calendar cal = getFlightLeg().getFirstWaypoint().getMostRelevantDateTime();
		DateTime dt = new DateTime(cal.getTimeInMillis(), cal.getTimeZone().getOffset(cal.getTimeInMillis()));
		return dt;
	}

	@Override
	public DateTime getEndDate() {
		Calendar cal = getFlightLeg().getLastWaypoint().getMostRelevantDateTime();
		DateTime dt = new DateTime(cal.getTimeInMillis(), cal.getTimeZone().getOffset(cal.getTimeInMillis()));
		return dt;
	}

}
