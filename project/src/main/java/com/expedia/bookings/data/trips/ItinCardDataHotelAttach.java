package com.expedia.bookings.data.trips;

import java.util.List;

import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.HotelSearchParams;

public class ItinCardDataHotelAttach extends ItinCardData {

	private TripFlight mTripFlight;
	private FlightLeg mFirstLeg;
	private FlightLeg mNextLeg;

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
		return HotelSearchParams.fromFlightParams(mFirstLeg, mNextLeg, numAdults, childTravelersInTrip);
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
