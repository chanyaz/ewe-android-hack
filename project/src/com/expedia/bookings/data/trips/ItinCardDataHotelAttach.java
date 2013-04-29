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
import com.expedia.bookings.tracking.OmnitureTracking;

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

	public SearchParams getSearchParams() {
		SearchParams searchParams = new SearchParams();

		// Where
		double latitude = mFirstLeg.getLastWaypoint().getAirport().getLatitude();
		double longitude = mFirstLeg.getLastWaypoint().getAirport().getLongitude();

		searchParams.setQuery(mFirstLeg.getLastWaypoint().getAirport().mCity);
		searchParams.setSearchType(SearchParams.SearchType.CITY);
		searchParams.setSearchLatLon(latitude, longitude);

		// When
		Calendar checkIn = mFirstLeg.getLastWaypoint().getBestSearchDateTime();
		Calendar checkOut = mNextLeg.getFirstWaypoint().getBestSearchDateTime();
		Calendar max = (Calendar) checkIn.clone();

		max.add(Calendar.DAY_OF_YEAR, 28);

		searchParams.setCheckInDate(checkIn);
		searchParams.setCheckOutDate(checkOut.after(max) ? max : checkOut);

		// Who
		searchParams.setNumAdults(1);
		searchParams.setChildren(null);

		return searchParams;
	}

	@Override
	public Intent getClickIntent(Context context) {
		OmnitureTracking.trackCrossSellItinToHotel(context);
		
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