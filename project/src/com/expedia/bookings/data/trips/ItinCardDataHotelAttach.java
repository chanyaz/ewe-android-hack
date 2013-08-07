package com.expedia.bookings.data.trips;

import java.util.Calendar;

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
		Calendar checkIn = mFirstLeg.getLastWaypoint().getBestSearchDateTime();
		Calendar checkOut = mNextLeg.getFirstWaypoint().getBestSearchDateTime();
		Calendar max = (Calendar) checkIn.clone();

		max.add(Calendar.DAY_OF_YEAR, 28);
		checkOut = checkOut.after(max) ? max : checkOut;

		searchParams.setCheckInDate(LocalDate.fromCalendarFields(checkIn));
		searchParams.setCheckOutDate(LocalDate.fromCalendarFields(checkOut));

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
