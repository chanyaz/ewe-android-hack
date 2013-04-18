package com.expedia.bookings.data.trips;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.SearchParams;

public class ItinCardDataHotelAttach extends ItinCardData {
	public ItinCardDataHotelAttach(TripComponent tripComponent) {
		super(tripComponent);
	}

	public SearchParams getSearchParams() {
		TripFlight flight = (TripFlight) getTripComponent();

		SearchParams searchParams = new SearchParams();

		// Where
		FlightTrip flightTrip = ((TripFlight) getTripComponent()).getFlightTrip();
		FlightLeg flightLeg = flightTrip.getLeg(0);

		if (flightLeg != null && flightLeg.getLastWaypoint() != null
				&& flightLeg.getLastWaypoint().getAirport() != null
				&& !TextUtils.isEmpty(flightLeg.getLastWaypoint().getAirport().mCity)) {

			double latitude = flightLeg.getLastWaypoint().getAirport().getLatitude();
			double longitude = flightLeg.getLastWaypoint().getAirport().getLongitude();

			searchParams.setQuery(flightLeg.getLastWaypoint().getAirport().mCity);
			searchParams.setSearchType(SearchParams.SearchType.CITY);
			searchParams.setSearchLatLon(latitude, longitude);
		}

		// When
		searchParams.setCheckInDate(flight.getStartDate().getCalendar());
		searchParams.setCheckOutDate(flight.getEndDate().getCalendar());

		// Who
		searchParams.setNumAdults(1);
		searchParams.setChildren(null);

		return searchParams;
	}

	@Override
	public Intent getClickIntent(Context context) {
		Db.setSearchParams(getSearchParams());

		Intent intent = new Intent(context, PhoneSearchActivity.class);
		intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true);

		return intent;
	}

	@Override
	public boolean hasDetailData() {
		return false;
	}
}