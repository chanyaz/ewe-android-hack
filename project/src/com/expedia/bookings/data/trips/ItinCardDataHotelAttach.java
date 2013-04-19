package com.expedia.bookings.data.trips;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.SearchParams;

public class ItinCardDataHotelAttach extends ItinCardData {
	private int mLegNumber;

	public ItinCardDataHotelAttach(TripFlight parent, int leg) {
		super(parent);

		mLegNumber = leg;
	}

	public FlightLeg getFlightLeg() {
		return ((TripFlight) getTripComponent()).getFlightTrip().getLeg(mLegNumber);
	}

	public SearchParams getSearchParams() {
		SearchParams searchParams = new SearchParams();

		// Where
		FlightLeg flightLeg = getFlightLeg();
		double latitude = flightLeg.getLastWaypoint().getAirport().getLatitude();
		double longitude = flightLeg.getLastWaypoint().getAirport().getLongitude();

		searchParams.setQuery(flightLeg.getLastWaypoint().getAirport().mCity);
		searchParams.setSearchType(SearchParams.SearchType.CITY);
		searchParams.setSearchLatLon(latitude, longitude);

		// When
		Calendar checkIn = flightLeg.getLastWaypoint().getMostRelevantDateTime();
		Calendar checkOut = flightLeg.getLastWaypoint().getMostRelevantDateTime();

		checkIn.setTimeZone(flightLeg.getLastWaypoint().getAirport().mTimeZone);
		checkOut.setTimeZone(flightLeg.getLastWaypoint().getAirport().mTimeZone);

		searchParams.setCheckInDate(checkIn);
		searchParams.setCheckOutDate(checkOut);

		// Who
		searchParams.setNumAdults(1);
		searchParams.setChildren(null);

		return searchParams;
	}

	@Override
	public Intent getClickIntent(Context context) {
		Db.setSearchParams(getSearchParams());

		Class<? extends Activity> targetClass = ExpediaBookingApp.useTabletInterface(context) ? SearchResultsFragmentActivity.class
				: PhoneSearchActivity.class;

		Intent intent = new Intent(context, targetClass);
		intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true);

		return intent;
	}

	@Override
	public boolean hasDetailData() {
		return false;
	}
}