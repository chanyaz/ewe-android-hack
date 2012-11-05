package com.expedia.bookings.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.fragment.FlightSearchParamsFragment;
import com.expedia.bookings.fragment.FlightSearchParamsFragment.FlightSearchParamsFragmentListener;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.json.JSONUtils;

public class FlightSearchOverlayActivity extends SherlockFragmentActivity implements FlightSearchParamsFragmentListener {

	public static final String EXTRA_SEARCH_PARAMS = "EXTRA_SEARCH_PARAMS";

	private FlightSearchParamsFragment mSearchParamsFragment;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View root = findViewById(android.R.id.content);
		root.setBackgroundColor(Color.argb(180, 0, 0, 0));

		setTitle(R.string.edit_search);

		if (savedInstanceState == null) {
			mSearchParamsFragment = FlightSearchParamsFragment.newInstance(Db.getFlightSearch().getSearchParams(),
					false);
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, mSearchParamsFragment,
					FlightSearchParamsFragment.TAG).commit();
		}
		else {
			mSearchParamsFragment = Ui.findSupportFragment(this, FlightSearchParamsFragment.TAG);
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onBackPressed() {
		if (!mSearchParamsFragment.onBackPressed()) {
			super.onBackPressed();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	private MenuItem mSearchMenuItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_flight_search, menu);
		mSearchMenuItem = ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.search);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		FlightSearchParams params = mSearchParamsFragment.getSearchParams();
		mSearchMenuItem.setVisible(params.isFilled());

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			FlightSearchParams params = mSearchParamsFragment.getSearchParams();
			if (!params.isFilled()) {
				throw new RuntimeException(
						"You should not be able to search unless you have filled out all the search params!");
			}
			else {
				Intent intent = new Intent();
				JSONUtils.putJSONable(intent, EXTRA_SEARCH_PARAMS, params);
				setResult(RESULT_OK, intent);
				finish();
			}
			return true;
		case android.R.id.home:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// FlightSearchParamsFragmentListener

	@Override
	public void onParamsChanged() {
		supportInvalidateOptionsMenu();
	}
}
