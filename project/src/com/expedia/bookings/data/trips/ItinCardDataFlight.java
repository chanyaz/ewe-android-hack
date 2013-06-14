package com.expedia.bookings.data.trips;

import java.util.Calendar;
import java.util.List;

import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.trips.ItinCardData.ConfirmationNumberable;
import com.google.android.gms.maps.model.LatLng;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;

public class ItinCardDataFlight extends ItinCardData implements ConfirmationNumberable {

	private int mLegNumber;
	private DateTime mEndDate;
	private DateTime mStartDate;

	public ItinCardDataFlight(TripFlight parent, int leg) {
		super(parent);

		mLegNumber = leg;

		setId(parent.getUniqueId() + ":" + mLegNumber);
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
			mStartDate = DateTime.newInstance(getFlightLeg().getFirstWaypoint().getMostRelevantDateTime());
		}
		return mStartDate;
	}

	@Override
	public DateTime getEndDate() {
		if (mEndDate == null) {
			mEndDate = DateTime.newInstance(getFlightLeg().getLastWaypoint().getMostRelevantDateTime());
		}
		return mEndDate;
	}

	@Override
	public boolean hasConfirmationNumber() {
		if (getTripComponent() != null && ((TripFlight) getTripComponent()).getConfirmations() != null
				&& ((TripFlight) getTripComponent()).getConfirmations().size() > 0) {
			return true;
		}
		return false;
	}

	@Override
	public String getFormattedConfirmationNumbers() {
		List<FlightConfirmation> confirmationNumbers = ((TripFlight) getTripComponent()).getConfirmations();
		if (confirmationNumbers != null) {
			return TextUtils.join(",  ", confirmationNumbers.toArray());
		}
		return null;
	}

	@Override
	public int getConfirmationNumberLabelResId() {
		return R.string.flight_confirmation_code_label;
	}

	@Override
	public LatLng getLocation() {
		long now = Calendar.getInstance().getTimeInMillis();
		Flight flight = getMostRelevantFlightSegment();
		Waypoint waypoint = flight.mOrigin.getMostRelevantDateTime().getTimeInMillis() > now ? flight.mOrigin
				: flight.getArrivalWaypoint();
		Airport airport = waypoint.getAirport();

		if (airport != null) {
			return new LatLng(airport.getLatitude(), airport.getLongitude());
		}

		return super.getLocation();
	}

	// Don't trust FlightStats' stats.  Just go off of start/end time.
	public boolean isEnRoute() {
		FlightLeg leg = getFlightLeg();

		Calendar now = Calendar.getInstance();
		return now.after(leg.getFirstWaypoint().getMostRelevantDateTime())
				&& now.before(leg.getLastWaypoint().getMostRelevantDateTime());
	}
}
