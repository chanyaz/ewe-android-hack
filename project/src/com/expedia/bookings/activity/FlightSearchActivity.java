package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.fragment.FlightSearchParamsFragment;
import com.expedia.bookings.fragment.FlightSearchParamsFragment.FlightSearchParamsFragmentListener;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class FlightSearchActivity extends FragmentActivity implements FlightSearchParamsFragmentListener {

	public static final String EXTRA_DATA_EXPIRED = "EXTRA_DATA_EXPIRED";

	public static final String ARG_USE_PRESET_SEARCH_PARAMS = "ARG_FROM_LAUNCH_WITH_SEARCH_PARAMS";

	private static final String INSTANCE_UPDATE_ON_RESUME = "INSTANCE_UPDATE_ON_RESUME";

	private FlightSearchParamsFragment mSearchParamsFragment;

	// Keeps track of whether we should update the search params fragment with
	// the latest HotelSearchParams on resume.  This is checked when the user leaves
	// this activity and comes back later (e.g., did a search).
	private boolean mUpdateOnResume = false;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		// Clear memory cache upon entry; we don't load any images from network
		// (besides destination images) so this just takes up unnecessary space
		// all throughout the flights part of the app.
		if (savedInstanceState == null) {
			Log.i("Clearing general-purpose image cache because we just launched flights");
			L2ImageCache.sGeneralPurpose.clearMemoryCache();
		}

		// On first launch, try to restore cached flight data (in this case, just for the search params)
		if (savedInstanceState == null && !Db.getFlightSearch().getSearchParams().isFilled()
				&& !getIntent().getBooleanExtra(ARG_USE_PRESET_SEARCH_PARAMS, false)) {
			Db.loadFlightSearchParamsFromDisk(this);
		}

		checkForDefaultDestinationImage(savedInstanceState);

		if (savedInstanceState != null) {
			mUpdateOnResume = savedInstanceState.getBoolean(INSTANCE_UPDATE_ON_RESUME);
		}

		setContentView(R.layout.activity_flight_search);
		getWindow().setBackgroundDrawable(null);

		if (savedInstanceState == null) {
			mSearchParamsFragment = FlightSearchParamsFragment.newInstance(Db.getFlightSearch().getSearchParams(),
					false);
			getSupportFragmentManager().beginTransaction().add(R.id.content, mSearchParamsFragment,
					FlightSearchParamsFragment.TAG).commit();
		}
		else {
			mSearchParamsFragment = Ui.findSupportFragment(this, FlightSearchParamsFragment.TAG);
		}

		if (savedInstanceState == null && getIntent().getBooleanExtra(EXTRA_DATA_EXPIRED, false)) {
			SimpleSupportDialogFragment df = SimpleSupportDialogFragment.newInstance(null,
					getString(R.string.error_data_expired));
			df.show(getSupportFragmentManager(), "dataExpiredDf");
		}

		//Actionbar
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.search_flights);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private static final int PHONE_FLIGHTS_DEFAULT_RES_ID = R.drawable.default_flights_background;

	private static final Object sDefaultsLock = new Object();

	// We load up the default backgrounds so they are ready to go later if/when we need them
	// this is important, as we need to load images before our memory load gets too heavy
	private void checkForDefaultDestinationImage(final Bundle savedInstanceState) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean hasReg = L2ImageCache.sDestination.hasInMemCache(PHONE_FLIGHTS_DEFAULT_RES_ID, false);
				boolean hasBlur = L2ImageCache.sDestination.hasInMemCache(PHONE_FLIGHTS_DEFAULT_RES_ID, true);

				if (savedInstanceState == null || !hasReg || !hasBlur) {
					synchronized (sDefaultsLock) {
						L2ImageCache.sDestination.getImage(getResources(), PHONE_FLIGHTS_DEFAULT_RES_ID, false);
						L2ImageCache.sDestination.getImage(getResources(), PHONE_FLIGHTS_DEFAULT_RES_ID, true);
					}
				}
			}
		}).start();
	}

	@Override
	protected void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightSearch(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mUpdateOnResume) {
			if (!Db.getFlightSearch().getSearchParams().isFilled()) {
				// F1073: If we got back here but the search params are not filled, that is probably an indicator
				// that the app crashed (because otherwise the search params *should* have data).  In this case,
				// attempt to reload the saved search params from disk.
				Db.loadFlightSearchParamsFromDisk(this);
				supportInvalidateOptionsMenu();
			}

			Db.getFlightSearch().getSearchParams().ensureValidDates();
			mSearchParamsFragment.setSearchParams(new FlightSearchParams(Db.getFlightSearch().getSearchParams()));
			mUpdateOnResume = false;
		}

		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isFinishing()) {
			L2ImageCache.sDestination.clearMemoryCache();
		}

		OmnitureTracking.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();

		// If the configuration isn't changing but we are stopping this activity, save the search params
		//
		// Due to not being able to tell a config change or not on earlier versions of Android, we just
		// always save.
		boolean configChange = false;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			configChange = isChangingConfigurations();
		}
		if (!configChange) {
			Db.saveFlightSearchParamsToDisk(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_UPDATE_ON_RESUME, mUpdateOnResume);
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
		getMenuInflater().inflate(R.menu.menu_flight_search, menu);
		mSearchMenuItem = ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.search);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		FlightSearchParams params = mSearchParamsFragment.getSearchParams(false);
		mSearchMenuItem.setVisible(params.isFilled());

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.search:
			FlightSearchParams params = mSearchParamsFragment.getSearchParams(true);
			if (!params.isFilled()) {
				throw new RuntimeException(
						"You should not be able to search unless you have filled out all the search params!");
			}
			else if (!params.hasDifferentAirports()) {
				DialogFragment df = SimpleSupportDialogFragment.newInstance(null,
						getString(R.string.error_same_flight_departure_arrival));
				df.show(getSupportFragmentManager(), "sameAirportsErrorDialog");
			}
			else {
				Log.i("Initial search requested!");

				Db.getFlightSearch().setSearchParams(params);
				startActivity(new Intent(FlightSearchActivity.this, FlightSearchResultsActivity.class));
				mUpdateOnResume = true;
				OmnitureTracking.markTrackNewSearchResultSet(true);
				AdTracker.trackFlightSearch();
			}
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
