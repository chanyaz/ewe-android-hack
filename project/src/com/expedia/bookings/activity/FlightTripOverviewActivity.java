package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.fragment.FlightTripOverviewFragment;
import com.expedia.bookings.utils.NavUtils;

public class FlightTripOverviewActivity extends SherlockFragmentActivity {

	public static final String EXTRA_TRIP_KEY = "EXTRA_TRIP_KEY";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Recover data if it was flushed from memory
		if (Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(this)) {
				NavUtils.onDataMissing(this);
				return;
			}
		}

		String tripKey = Db.getFlightSearch().getSelectedFlightTrip().getProductKey();
		
		if (savedInstanceState == null) {
			FlightTripOverviewFragment fragment = FlightTripOverviewFragment.newInstance(tripKey);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(android.R.id.content, fragment, "overviewFragment");
			ft.commit();
		}

		FlightTrip trip = Db.getFlightSearch().getFlightTrip(tripKey);
		String cityName = trip.getLeg(0).getLastWaypoint().getAirport().mCity;
		String yourTripToStr = String.format(getString(R.string.your_trip_to_TEMPLATE), cityName);
		
		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(yourTripToStr);
	}
}