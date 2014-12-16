package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CheckoutDataLoader;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.fragment.HotelOverviewFragment;
import com.expedia.bookings.fragment.HotelOverviewFragment.BookingOverviewFragmentListener;
import com.expedia.bookings.fragment.LoginFragment.LogInListener;
import com.expedia.bookings.fragment.WalletFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.SlideToWidget.ISlideToListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.ViewUtils;
import com.squareup.otto.Subscribe;

public class HotelOverviewActivity extends FragmentActivity implements BookingOverviewFragmentListener,
	LogInListener, ISlideToListener {

	public static final String STATE_TAG_LOADED_DB_INFO = "STATE_TAG_LOADED_DB_INFO";

	//We only want to load from disk once: when the activity is first started
	private boolean mLoadedDbInfo = false;

	private HotelOverviewFragment mBookingOverviewFragment;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!ExpediaBookingApp.useTabletInterface(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// #1106: Don't continue to load onCreate() as
			// we're just about to recreate the activity
			if (!getResources().getBoolean(R.bool.portrait)) {
				return;
			}
		}

		if (Db.getTripBucket().getHotel() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		if (savedInstanceState != null) {
			mLoadedDbInfo = savedInstanceState.getBoolean(STATE_TAG_LOADED_DB_INFO, false);
		}

		loadCachedData(false);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		setContentView(R.layout.activity_booking_overview);

		mBookingOverviewFragment = Ui.findSupportFragment(this, R.id.booking_overview_fragment);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Events.register(this);
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Events.unregister(this);
		OmnitureTracking.onPause();

		if (isFinishing()) {
			Db.getTripBucket().getHotel().clearCheckoutData();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		out.putBoolean(STATE_TAG_LOADED_DB_INFO, mLoadedDbInfo);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}

	@Override
	public void onBackPressed() {
		if (mBookingOverviewFragment.isInCheckout()) {
			mBookingOverviewFragment.endCheckout();
			supportInvalidateOptionsMenu();

			return;
		}

		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (WalletFragment.isRequestCodeFromWalletFragment(requestCode)) {
			mBookingOverviewFragment.onActivityResult(requestCode, resultCode, data);
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_booking_overview, menu);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		Property property;
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return false;
		}
		else {
			property = hotel.getProperty();
		}

		HotelUtils.setupActionBarHotelNameAndRating(this, property);

		final MenuItem checkoutItem = menu.findItem(R.id.menu_checkout);
		Button tv = Ui.inflate(this, R.layout.actionbar_checkout, null);
		ViewUtils.setAllCaps(tv);
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected(checkoutItem);
			}
		});

		checkoutItem.setActionView(tv);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			onBackPressed();
			return true;
		}
		case R.id.menu_checkout: {
			mBookingOverviewFragment.startCheckout();
			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu != null) {
			MenuItem checkoutItem = menu.findItem(R.id.menu_checkout);
			if (mBookingOverviewFragment != null && checkoutItem != null) {
				boolean visible = !mBookingOverviewFragment.isInCheckout();
				Log.d("Setting Checkout Button Visibility: visible=" + visible);
				checkoutItem.setVisible(visible);
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	// Private methods

	private void loadCachedData(boolean wait) {

		if (!mLoadedDbInfo) {
			CheckoutDataLoader.CheckoutDataLoadedListener listener = new CheckoutDataLoader.CheckoutDataLoadedListener() {
				@Override
				public void onCheckoutDataLoaded(boolean wasSuccessful) {
					mLoadedDbInfo = wasSuccessful;
					Runnable refreshDataRunner = new Runnable() {
						@Override
						public void run() {
							if (mBookingOverviewFragment != null) {
								mBookingOverviewFragment.refreshData();
							}
						}
					};
					runOnUiThread(refreshDataRunner);
				}

			};
			CheckoutDataLoader.getInstance().loadCheckoutData(this, true, true, listener, wait);
		}
	}

	//BookingOverviewFragmentListener implementation

	@Override
	public void checkoutStarted() {
		supportInvalidateOptionsMenu();
	}

	@Override
	public void checkoutEnded() {
		supportInvalidateOptionsMenu();
	}

	// LogInListener implementation

	@Override
	public void onLoginStarted() {
	}

	@Override
	public void onLoginCompleted() {
		mBookingOverviewFragment.onLoginCompleted();
	}

	@Override
	public void onLoginFailed() {
	}

	// ISlideToListener implementation

	@Override
	public void onSlideStart() {
	}

	@Override
	public void onSlideAllTheWay() {

		if (!BookingInfoUtils
			.migrateRequiredCheckoutDataToDbBillingInfo(this, LineOfBusiness.HOTELS, Db.getTravelers().get(0), true)) {
			if (mBookingOverviewFragment != null) {
				mBookingOverviewFragment.resetSlider();
			}
			Ui.showToast(this, R.string.please_enter_a_valid_email_address);
			mBookingOverviewFragment
				.startCheckout(false, false);//This will update all of our views (and re-validate everything).
			return;
		}

		//Seal the deal
		startActivity(new Intent(this, HotelBookingActivity.class));
	}

	@Override
	public void onSlideAbort() {
	}

	//////////////////////////////////////////////////////////////////////////
	// Otto event subscriptions
	//
	// Note: If you're on anything but the 3.2.2 branch this can be removed;
	// it's only here while SimpleCallbackDialogFragment has no easy way of
	// listening from a child Fragment.

	@Subscribe
	public void onSimpleDialogClick(Events.SimpleCallBackDialogOnClick event) {
		mBookingOverviewFragment.onSimpleDialogClick(event);
	}

	@Subscribe
	public void onSimpleDialogCancel(Events.SimpleCallBackDialogOnCancel event) {
		mBookingOverviewFragment.onSimpleDialogCancel(event);
	}
}
