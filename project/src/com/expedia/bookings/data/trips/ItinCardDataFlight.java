package com.expedia.bookings.data.trips;

import java.util.Calendar;

import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.FlightLeg;

public class ItinCardDataFlight extends ItinCardData {

	private int mLegNumber;
	private DateTime mEndDate;
	private DateTime mStartDate;

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
		if (mStartDate == null) {
			Calendar cal = getFlightLeg().getFirstWaypoint().getMostRelevantDateTime();
			mStartDate = new DateTime(cal.getTimeInMillis(), cal.getTimeZone().getOffset(cal.getTimeInMillis()));
		}
		return mStartDate;
	}

	@Override
	public DateTime getEndDate() {
		if (mEndDate == null) {
			Calendar cal = getFlightLeg().getLastWaypoint().getMostRelevantDateTime();
			mEndDate = new DateTime(cal.getTimeInMillis(), cal.getTimeZone().getOffset(cal.getTimeInMillis()));
		}
		return mEndDate;
	}

}
