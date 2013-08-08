package com.expedia.bookings.data.trips;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.HotelSearchParams;

public class ItinCardDataHotelAttach extends ItinCardData {
	private FlightLeg mFirstLeg;
	private FlightLeg mNextLeg;

	public ItinCardDataHotelAttach(TripFlight parent, FlightLeg firstLeg, FlightLeg nextLeg) {
		super(parent);

		mFirstLeg = firstLeg;
		mNextLeg = nextLeg;
	}

	public FlightLeg getFlightLeg() {
		return mFirstLeg;
	}

	public HotelSearchParams getSearchParams() {
		HotelSearchParams searchParams = new HotelSearchParams();

		// Where
		double latitude = mFirstLeg.getLastWaypoint().getAirport().getLatitude();
		double longitude = mFirstLeg.getLastWaypoint().getAirport().getLongitude();

		searchParams.setQuery(mFirstLeg.getLastWaypoint().getAirport().mCity);
		searchParams.setSearchType(HotelSearchParams.SearchType.CITY);
		searchParams.setSearchLatLon(latitude, longitude);

		// When
		LocalDate checkInDate = LocalDate.fromCalendarFields(mFirstLeg.getLastWaypoint().getBestSearchDateTime());
		LocalDate checkOutDate = LocalDate.fromCalendarFields(mNextLeg.getFirstWaypoint().getBestSearchDateTime());

		LocalDate maxCheckOutDate = checkInDate.plusDays(28);
		checkOutDate = checkOutDate.isAfter(maxCheckOutDate) ? maxCheckOutDate : checkOutDate;

		searchParams.setCheckInDate(checkInDate);
		searchParams.setCheckOutDate(checkOutDate);

		// Who
		searchParams.setNumAdults(1);
		searchParams.setChildren(null);

		return searchParams;
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
