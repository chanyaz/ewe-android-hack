package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.fragment.BookingFormFragment;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.BookingInfoFragment;
import com.expedia.bookings.fragment.BookingInfoFragment.BookingInfoFragmentListener;
import com.expedia.bookings.fragment.RoomsAndRatesFragment.RoomsAndRatesFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleDialogFragment;
import com.mobiata.android.validation.ValidationError;

// This is the TABLET booking activity for hotels.

public class RoomsAndRatesFragmentActivity extends FragmentActivity implements RoomsAndRatesFragmentListener,
		BookingInfoFragmentListener {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final String BOOKING_DOWNLOAD_KEY = RoomsAndRatesFragmentActivity.class.getName() + ".BOOKING";

	public static final String EXTRA_SPECIFIC_RATE = "EXTRA_SPECIFIC_RATE";

	private static final long RESUME_TIMEOUT = 1000 * 60 * 20; // 20 minutes

	//////////////////////////////////////////////////////////////////////////
	// Member vars

	private Context mContext;

	private BookingInfoFragment mBookingInfoFragment;

	private long mLastResumeTime = -1;

	private ActivityKillReceiver mKillReciever;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// #13365: If the Db expired, finish out of this activity
		if (Db.getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		mContext = this;

		setContentView(R.layout.activity_booking_fragment);

		mBookingInfoFragment = Ui.findSupportFragment(this, getString(R.string.tag_booking_info));

		// Need to set this BG from code so we can make it just repeat vertically
		findViewById(R.id.search_results_list_shadow).setBackgroundDrawable(LayoutUtils.getDividerDrawable(this));

		if (savedInstanceState == null) {
			String referrer = getIntent().getBooleanExtra(EXTRA_SPECIFIC_RATE, false) ? "App.Hotels.ViewSpecificRoom"
					: "App.Hotels.ViewAllRooms";

			Tracker.trackAppHotelsRoomsRates(this, Db.getSelectedProperty(), referrer);
		}

		mKillReciever = new ActivityKillReceiver(this);
		mKillReciever.onCreate();
	}

	@TargetApi(11)
	@Override
	protected void onStart() {
		super.onStart();

		// Configure the ActionBar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setTitle(R.string.booking_information_title);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// #14135, set a 1 hour timeout on this screen
		if (mLastResumeTime != -1 && mLastResumeTime + RESUME_TIMEOUT < Calendar.getInstance().getTimeInMillis()) {
			finish();
			return;
		}
		mLastResumeTime = Calendar.getInstance().getTimeInMillis();
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

	@TargetApi(11)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_fragment_standard, menu);

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
			Intent intent = new Intent(this, TabletAboutActivity.class);
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
		Db.setSelectedRate(rate);

		mBookingInfoFragment.notifyRateSelected();
	}

	//////////////////////////////////////////////////////////////////////////
	// BookingInfoFragmentListener

	@Override
	public void onEnterBookingInfoClick() {
		Intent intent = new Intent(this, BookingFragmentActivity.class);
		startActivity(intent);
	}
}
