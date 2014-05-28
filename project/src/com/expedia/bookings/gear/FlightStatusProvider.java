package com.expedia.bookings.gear;

import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.flightlib.data.Delay;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;


public class FlightStatusProvider {


	public static final String CANCELLED = "Cancelled";
	public static final String DELAYED = "Delayed";
	public static final String ONTIME = "On-time";

	public Flight getMostRelevantFlightSegmentGear(FlightLeg fLeg) {

		Calendar now = Calendar.getInstance();
		List<Flight> segments = fLeg.getSegments();

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


	private int getDelayForWaypoint(Waypoint wp) {
		Delay delay = wp.getDelay();
		if (delay.mDelayType == Delay.DELAY_GATE_ACTUAL || delay.mDelayType == Delay.DELAY_GATE_ESTIMATED) {
			return delay.mDelay;
		}
		else {
			return 0;
		}
	}

	public String getFlightStatus(Flight flight) {

		DateTime departure = new DateTime(flight.mOrigin.getMostRelevantDateTime());
		DateTime now = DateTime.now();

		if (flight.isRedAlert()) {
			return CANCELLED;
		}
		else {
			Waypoint summaryWaypoint = null;


			if (departure.isBefore(now) && (flight.mFlightHistoryId != -1)) {
				//flight in progress AND we have FS data, show arrival info
				int delay = getDelayForWaypoint(flight.getArrivalWaypoint());

				if (delay > 0) {
					return DELAYED;
				}
				else {
					return ONTIME;
				}


			}
			else if (JodaUtils.daysBetween(now, departure) > 3 || flight.mFlightHistoryId == -1) {
				return ONTIME;
			}
			else {
				//Less than 72 hours in the future and has FS data
				int delay = getDelayForWaypoint(flight.mOrigin);

				if (delay > 0) {
					return DELAYED;
				}
				else {
					return ONTIME;
				}
			}

		}
	}

}