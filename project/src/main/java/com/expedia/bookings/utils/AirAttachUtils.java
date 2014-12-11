package com.expedia.bookings.utils;

import java.util.List;

import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.trips.TripFlight;

public class AirAttachUtils {

	public static HotelSearchParams generateHotelSearchParamsFromItinData(TripFlight tripFlight,
																		  FlightLeg firstLeg, FlightLeg secondLeg) {
		List<ChildTraveler> childTravelersInTrip = tripFlight.getChildTravelers();
		int numAdults = tripFlight.getTravelers().size() - childTravelersInTrip.size();
		return HotelSearchParams.fromFlightParams(firstLeg, secondLeg, numAdults, childTravelersInTrip);
	}

}
