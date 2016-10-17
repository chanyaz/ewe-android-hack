package com.expedia.bookings.utils;

import java.util.List;

import android.content.Context;

import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.trips.TripFlight;
import com.mobiata.android.util.AndroidUtils;

public class HotelCrossSellUtils {

	public static HotelSearchParams generateHotelSearchParamsFromItinData(TripFlight tripFlight,
		FlightLeg firstLeg, FlightLeg secondLeg) {
		List<ChildTraveler> childTravelersInTrip = tripFlight.getChildTravelers();
		int numAdults = tripFlight.getTravelers().size() - childTravelersInTrip.size();
		String regionId = tripFlight.getDestinationRegionId();
		return HotelSearchParams.fromFlightParams(regionId, firstLeg, secondLeg, numAdults, childTravelersInTrip);
	}

	public static void deepLinkHotels(final Context context, final HotelSearchParams hotelSearchParams) {
		if (AndroidUtils.isTablet(context)) {
			final SearchParams searchParams = SearchParams.fromHotelSearchParams(hotelSearchParams);
			NavUtils.goToTabletResults(context, searchParams, LineOfBusiness.HOTELS);
		}
		else {
			NavUtils.goToHotels(context, hotelSearchParams);
		}
	}

}
