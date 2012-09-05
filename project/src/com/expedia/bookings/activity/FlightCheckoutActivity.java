package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.fragment.FlightCheckoutFragment;
import com.expedia.bookings.fragment.SignInFragment.SignInFragmentListener;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;

public class FlightCheckoutActivity extends SherlockFragmentActivity implements SignInFragmentListener{
	FlightCheckoutFragment mCheckoutFragment;
	
	private static final String FRAG_TAG = "FRAG_TAG";
	
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
		
		mCheckoutFragment = Ui.findSupportFragment(this, FRAG_TAG);
		if (mCheckoutFragment == null) {
			mCheckoutFragment = FlightCheckoutFragment.newInstance();
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(android.R.id.content, mCheckoutFragment, FRAG_TAG);
			ft.commit();
		}

		FlightTrip trip = Db.getFlightSearch().getFlightTrip(tripKey);
		String cityName = trip.getLeg(0).getLastWaypoint().getAirport().mCity;
		String yourTripToStr = String.format(getString(R.string.your_trip_to_TEMPLATE), cityName);
		
		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(yourTripToStr);
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	// SignInFragmentListener

	@Override
	public void onLoginStarted() {
		// Do nothing?
	}

	@Override
	public void onLoginCompleted() {
		mCheckoutFragment.onLoginCompleted();
	}

	@Override
	public void onLoginFailed() {
		// TODO: Update UI to show that we're no longer logged in
	}
}