package com.expedia.bookings.data.trips;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.DeprecatedHotelSearchParams;
import com.expedia.bookings.utils.HotelCrossSellUtils;

public class ItinCardDataAirAttach extends ItinCardData {

	private TripFlight mTripFlight;
	private FlightLeg mFirstLeg;
	private FlightLeg mNextLeg;

	public ItinCardDataAirAttach(TripFlight parent, FlightLeg firstLeg, FlightLeg nextLeg) {
		super(parent);

		mTripFlight = parent;
		mFirstLeg = firstLeg;
		mNextLeg = nextLeg;
	}

	public FlightLeg getFlightLeg() {
		return mFirstLeg;
	}

	public DeprecatedHotelSearchParams getSearchParams() {
		return HotelCrossSellUtils.generateHotelSearchParamsFromItinData(mTripFlight, mFirstLeg, mNextLeg);
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
