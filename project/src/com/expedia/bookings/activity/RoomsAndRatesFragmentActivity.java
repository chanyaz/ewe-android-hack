package com.expedia.bookings.activity;

import org.joda.time.DateTime;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.fragment.BookingInfoFragment;
import com.expedia.bookings.fragment.BookingInfoFragment.BookingInfoFragmentListener;
import com.expedia.bookings.fragment.RoomsAndRatesFragment;
import com.expedia.bookings.fragment.RoomsAndRatesFragment.RoomsAndRatesFragmentListener;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class RoomsAndRatesFragmentActivity extends SherlockFragmentActivity implements RoomsAndRatesFragmentListener,
		BookingInfoFragmentListener {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final String BOOKING_DOWNLOAD_KEY = RoomsAndRatesFragmentActivity.class.getName() + ".BOOKING";

	public static final String EXTRA_SPECIFIC_RATE = "EXTRA_SPECIFIC_RATE";

	private static final long RESUME_TIMEOUT = 20 * DateUtils.MINUTE_IN_MILLIS;

	//////////////////////////////////////////////////////////////////////////
	// Member vars

	private RoomsAndRatesFragment mRoomsAndRatesFragment;
	private BookingInfoFragment mBookingInfoFragment;

	private DateTime mLastResumeTime;

	private ActivityKillReceiver mKillReciever;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// #13365: If the Db expired, finish out of this activity
		if (Db.getHotelSearch().getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		setContentView(R.layout.activity_booking_fragment);

		getWindow().setBackgroundDrawable(null);

		mRoomsAndRatesFragment = Ui.findSupportFragment(this, getString(R.string.tag_rooms_and_rates));
		mBookingInfoFragment = Ui.findSupportFragment(this, getString(R.string.tag_booking_info));

		// Need to set this BG from code so we can make it just repeat vertically
		findViewById(R.id.search_results_list_shadow).setBackgroundDrawable(LayoutUtils.getDividerDrawable(this));

		if (savedInstanceState == null) {
			String referrer = getIntent().getBooleanExtra(EXTRA_SPECIFIC_RATE, false) ? "App.Hotels.ViewSpecificRoom"
					: "App.Hotels.ViewAllRooms";

			OmnitureTracking.trackAppHotelsRoomsRates(this, Db.getHotelSearch().getSelectedProperty(), referrer);
		}

		mKillReciever = new ActivityKillReceiver(this);
		mKillReciever.onCreate();
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Configure the ActionBar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setTitle(R.string.booking_information_title);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// #14135, set a 1 hour timeout on this screen
		if (JodaUtils.isExpired(mLastResumeTime, RESUME_TIMEOUT)) {
			finish();
			return;
		}
		mLastResumeTime = DateTime.now();

		mRoomsAndRatesFragment.notifyAvailabilityLoaded();

		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReciever != null) {
			mKillReciever.onDestroy();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// ActionBar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_fragment_standard, menu);

		DebugMenu.onCreateOptionsMenu(this, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.menu_about: {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		}
		}

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// RoomsAndRatesFragmentListener

	@Override
	public void onRateSelected(Rate rate) {
		String selectedId = Db.getHotelSearch().getSelectedPropertyId();
		Db.getHotelSearch().getAvailability(selectedId).setSelectedRate(rate);

		mBookingInfoFragment.notifyRateSelected();
	}

	@Override
	public void noRatesAvailable() {
		mBookingInfoFragment.notifyNoRates();
	}

	//////////////////////////////////////////////////////////////////////////
	// BookingInfoFragmentListener

	@Override
	public void onEnterBookingInfoClick() {
		Intent intent = new Intent(this, HotelOverviewActivity.class);
		startActivity(intent);
	}
}
