package com.expedia.bookings.data.trips;

import java.util.List;

import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.HotelSearchParams;

public class ItinCardDataHotelAttach extends ItinCardData {

	private final TripFlight mTripFlight;
	private final FlightLeg mFirstLeg;
	private final FlightLeg mNextLeg;

	public ItinCardDataHotelAttach(TripFlight parent, FlightLeg firstLeg, FlightLeg nextLeg) {
		super(parent);

		mTripFlight = parent;
		mFirstLeg = firstLeg;
		mNextLeg = nextLeg;
	}

	public String getTripId() {
		return mTripFlight.getParentTrip().getTripId();
	}

	public FlightLeg getFlightLeg() {
		return mFirstLeg;
	}

	public HotelSearchParams getSearchParams() {
		List<ChildTraveler> childTravelersInTrip = mTripFlight.getChildTravelers();
		int numAdults = mTripFlight.getTravelers().size() - childTravelersInTrip.size();
		String regionId = mTripFlight.getDestinationRegionId();
		return HotelSearchParams.fromFlightParams(regionId, mFirstLeg, mNextLeg, numAdults, childTravelersInTrip);
	}

	@Override
	public boolean hasSummaryData() {
		return false;
	}

	@Override
	public boolean hasDetailData() {
		return false;
	}

}
