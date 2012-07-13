package com.expedia.bookings.activity;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.fragment.FlightFilterDialogFragment;
import com.expedia.bookings.fragment.FlightListFragment;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightAdapter.FlightAdapterListener;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class FlightSearchResultsActivity extends SherlockFragmentActivity implements FlightAdapterListener {

	public static final String EXTRA_LEG_POSITION = "EXTRA_LEG_POSITION";

	private FlightListFragment mListFragment;

	// Current leg being displayed
	private int mLegPosition;

	private TextView mTitleTextView;
	private TextView mSubtitleTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLegPosition = getIntent().getIntExtra(EXTRA_LEG_POSITION, 0);

		mListFragment = Ui.findSupportFragment(this, FlightListFragment.TAG);
		if (mListFragment == null) {
			mListFragment = FlightListFragment.newInstance(mLegPosition);
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, mListFragment, FlightListFragment.TAG).commit();
		}

		// DELETE EVENTUALLY: For now, just set the header to always be SF
		mListFragment.setHeaderDrawable(getResources().getDrawable(R.drawable.san_francisco));

		// Configure the custom action bar view
		ActionBar actionBar = getSupportActionBar();
		actionBar.setCustomView(R.layout.action_bar_flight_results);
		View customView = actionBar.getCustomView();
		mTitleTextView = Ui.findView(customView, R.id.title_text_view);
		mSubtitleTextView = Ui.findView(customView, R.id.subtitle_text_view);
		actionBar.setDisplayShowCustomEnabled(true);

		// Configure the title based on which leg the user is selecting
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		int titleStrId = (mLegPosition == 0) ? R.string.outbound_TEMPLATE : R.string.inbound_TEMPLATE;
		String airportCode = (mLegPosition == 0) ? params.getArrivalAirportCode() : params.getDepartureAirportCode();
		mTitleTextView.setText(getString(titleStrId, FlightStatsDbUtils.getAirport(airportCode).mCity));

		// Configure subtitle based on which user the leg is selecting
		Date date = (mLegPosition == 0) ? params.getDepartureDateWithDefault() : params.getReturnDate();
		mSubtitleTextView.setText(android.text.format.DateFormat.getMediumDateFormat(this).format(
				date.getCalendar().getTime()));

		// Enable the home button on the action bar
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isFinishing()) {
			// Clear out the selected leg if the user is exiting the Activity
			Db.getFlightSearch().setSelectedLeg(mLegPosition, null);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_flight_results, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Push user back to search page if they hit the home button
			Intent intent = new Intent(this, FlightSearchActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_CLEAR_TASK
					+ Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;
		case R.id.menu_filter:
			showFilterDialog();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// Filter dialog

	public void showFilterDialog() {
		FlightFilterDialogFragment fragment = FlightFilterDialogFragment.newInstance(mLegPosition);
		fragment.show(getSupportFragmentManager(), FlightFilterDialogFragment.TAG);
	}

	//////////////////////////////////////////////////////////////////////////
	// FlightAdapterListener

	@Override
	public void onDetailsClick(FlightTrip trip, FlightLeg leg, int position) {
		Intent intent = new Intent(this, FlightDetailsActivity.class);
		intent.putExtra(FlightDetailsActivity.EXTRA_TRIP_KEY, trip.getProductKey());
		intent.putExtra(FlightDetailsActivity.EXTRA_LEG_POSITION, mLegPosition);
		startActivity(intent);
	}

	@Override
	public void onSelectClick(FlightTrip trip, FlightLeg leg, int position) {
		Db.getFlightSearch().setSelectedLeg(mLegPosition, leg);

		NavUtils.onFlightLegSelected(this);
	}
}
