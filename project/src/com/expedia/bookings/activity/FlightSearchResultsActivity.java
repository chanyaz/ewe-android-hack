package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.fragment.FlightListFragment;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightAdapter.FlightAdapterListener;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class FlightSearchResultsActivity extends SherlockFragmentActivity implements FlightAdapterListener {

	public static final String EXTRA_LEG_POSITION = "EXTRA_LEG_POSITION";

	private FlightListFragment mListFragment;

	// Current leg being displayed
	private int mLegPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLegPosition = getIntent().getIntExtra(EXTRA_LEG_POSITION, 0);

		mListFragment = Ui.findOrAddSupportFragment(this, FlightListFragment.class, "listFragment");
		mListFragment.setLegPosition(mLegPosition);
		mListFragment.setFlights(Db.getFlightSearch().getTrips(mLegPosition));

		// DELETE EVENTUALLY: For now, just set the header to always be SF
		mListFragment.setHeaderDrawable(getResources().getDrawable(R.drawable.san_francisco));

		// Configure the title based on which leg the user is selecting
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		int titleStrId = (mLegPosition == 0) ? R.string.outbound_TEMPLATE : R.string.inbound_TEMPLATE;
		String airportCode = (mLegPosition == 0) ? params.getArrivalAirportCode() : params.getDepartureAirportCode();
		setTitle(getString(titleStrId, FlightStatsDbUtils.getAirport(airportCode).mCity));

		// Enable the home button on the action bar
		if (AndroidUtils.getSdkVersion() >= 11) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isFinishing()) {
			// Clear out the selected leg if the user is exiting the Activity
			Db.getFlightSearch().setSelectedLeg(mLegPosition, null);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Push user back to search page if they hit the home button
			Intent intent = new Intent(this, FlightSearchActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// FlightAdapterListener

	@Override
	public void onDetailsClick(FlightTrip trip, FlightLeg leg, int position) {
		// TODO: This should probably not be based on array position/leg position.
		Intent intent = new Intent(this, FlightDetailsActivity.class);
		intent.putExtra(FlightDetailsActivity.EXTRA_STARTING_POSITION, position);
		intent.putExtra(FlightDetailsActivity.EXTRA_LEG_POSITION, mLegPosition);
		startActivity(intent);
	}

	@Override
	public void onSelectClick(FlightTrip trip, FlightLeg leg, int position) {
		FlightSearch search = Db.getFlightSearch();
		search.setSelectedLeg(mLegPosition, leg);

		if (mLegPosition + 1 < search.getSearchParams().getQueryLegCount()) {
			// If the user hasn't selected all legs yet, push them to select the next leg
			Intent intent = new Intent(this, FlightSearchResultsActivity.class);
			intent.putExtra(EXTRA_LEG_POSITION, mLegPosition + 1);
			startActivity(intent);
		}
		else {
			// TODO: If the user has selected all legs, go to checkout screen
			Toast.makeText(this, "TODO: All legs selected, implement checkout screen", Toast.LENGTH_SHORT).show();
		}
	}
}
