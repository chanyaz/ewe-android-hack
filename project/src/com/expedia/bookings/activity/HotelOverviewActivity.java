package com.expedia.bookings.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CheckoutDataLoader;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.HotelOverviewFragment;
import com.expedia.bookings.fragment.HotelOverviewFragment.BookingOverviewFragmentListener;
import com.expedia.bookings.fragment.LoginFragment.LogInListener;
import com.expedia.bookings.fragment.WalletFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.SlideToWidget.ISlideToListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.ViewUtils;

public class HotelOverviewActivity extends SherlockFragmentActivity implements BookingOverviewFragmentListener,
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

		if (Db.getHotelSearch().getSelectedProperty() == null) {
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
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();

		if (isFinishing()) {
			Db.getHotelSearch().setCreateTripResponse(null);
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
		getSupportMenuInflater().inflate(R.menu.menu_booking_overview, menu);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		ViewGroup titleView = (ViewGroup) getLayoutInflater().inflate(R.layout.actionbar_hotel_name_with_stars, null);

		Property property = Db.getHotelSearch().getSelectedProperty();
		if (property == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return false;
		}

		((TextView) titleView.findViewById(R.id.title)).setText(property.getName());
		((RatingBar) titleView.findViewById(R.id.rating)).setRating((float) property.getHotelRating());

		actionBar.setCustomView(titleView);

		final MenuItem checkoutItem = menu.findItem(R.id.menu_checkout);
		Button tv = (Button) getLayoutInflater().inflate(R.layout.actionbar_checkout, null);
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
		BillingInfo billingInfo = Db.getBillingInfo();

		billingInfo.save(this);

		// TODO: Does the code below need to run?  I'm really unsure.  Someone should look into it at some point.

		//TODO: This block shouldn't happen. Currently the mocks pair phone number with travelers, but the BillingInfo object contains phone info.
		//We need to wait on API updates to either A) set phone number as a billing phone number or B) take a bunch of per traveler phone numbers
		Traveler traveler = Db.getTravelers().get(0);
		billingInfo.setFirstName(traveler.getFirstName());
		billingInfo.setLastName(traveler.getLastName());
		billingInfo.setTelephone(traveler.getPhoneNumber());
		billingInfo.setTelephoneCountryCode(traveler.getPhoneCountryCode());

		//TODO: This also shouldn't happen, we should expect billingInfo to have a valid email address at this point...
		if (TextUtils.isEmpty(billingInfo.getEmail()) || (User.isLoggedIn(this) && Db.getUser() != null
				&& Db.getUser().getPrimaryTraveler() != null
				&& !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail()) && Db.getUser()
				.getPrimaryTraveler().getEmail().compareToIgnoreCase(billingInfo.getEmail()) != 0)) {
			String email = traveler.getEmail();
			if (TextUtils.isEmpty(email)) {
				email = Db.getUser().getPrimaryTraveler().getEmail();
			}
			billingInfo.setEmail(email);
		}

		startActivity(new Intent(this, HotelBookingActivity.class));
	}

	@Override
	public void onSlideAbort() {
	}
}
