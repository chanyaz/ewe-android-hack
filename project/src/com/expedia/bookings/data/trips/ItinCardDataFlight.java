package com.expedia.bookings.data.trips;

import java.util.Calendar;
import java.util.List;

import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.FlightLeg;
import com.mobiata.flightlib.data.Flight;

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

	// the most relevant flight segment is the currently-active segment if it has not yet landed,
	// otherwise, it is the next segment to take off
	// UNLESS there is a later flight that has been canceled
	public Flight getMostRelevantFlightSegment() {
		Calendar now = Calendar.getInstance();
		List<Flight> segments = getFlightLeg().getSegments();
		Flight relevantSegment = null;
		for (Flight segment : segments) {
			if (relevantSegment == null) {
				if (segment.mOrigin.getMostRelevantDateTime().after(now)
						|| segment.getArrivalWaypoint().getMostRelevantDateTime().after(now)) {
					relevantSegment = segment;
				}
			}
			else if (Flight.STATUS_CANCELLED.equals(segment.mStatusCode)) {
				return segment;
			}
		}
		
		if (relevantSegment != null) {
			return relevantSegment;
		}
		else {
			return segments.get(segments.size() - 1);
		}
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
