package com.expedia.bookings.utils;

import java.util.List;

import android.content.Context;

import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.DeprecatedHotelSearchParams;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.utils.navigation.HotelNavUtils;

public class HotelCrossSellUtils {

	public static DeprecatedHotelSearchParams generateHotelSearchParamsFromItinData(TripFlight tripFlight,
		FlightLeg firstLeg, FlightLeg secondLeg) {
		List<ChildTraveler> childTravelersInTrip = tripFlight.getChildTravelers();
		int numAdults = tripFlight.getTravelers().size() - childTravelersInTrip.size();
		String regionId = tripFlight.getDestinationRegionId();
		return DeprecatedHotelSearchParams
			.fromFlightParams(regionId, firstLeg, secondLeg, numAdults, childTravelersInTrip);
	}

	public static void deepLinkHotels(final Context context, final DeprecatedHotelSearchParams hotelSearchParams) {
		HotelNavUtils.goToHotels(context, hotelSearchParams);
	}

}
