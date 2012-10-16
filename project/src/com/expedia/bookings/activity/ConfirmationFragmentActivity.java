package com.expedia.bookings.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.BookingConfirmationFragment.BookingConfirmationFragmentListener;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;

public class ConfirmationFragmentActivity extends SherlockFragmentMapActivity implements
		BookingConfirmationFragmentListener {

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		if (AndroidUtils.isTablet(this)) {
			setTheme(R.style.Theme_Tablet_Confirmation);
		}
		else {
			setTheme(R.style.Theme_Phone);
		}

		if (savedInstanceState == null) {
			if (ConfirmationUtils.hasSavedConfirmationData(this)) {
				// Load saved data from disk
				if (!loadSavedConfirmationData()) {
					// If we failed to load the saved confirmation data, we should
					// delete the file and go back (since we are only here if we were called
					// directly from a startup).
					ConfirmationUtils.deleteSavedConfirmationData(this);
					finish();
				}
			}
			else {
				// Start a background thread to save this data to the disk
				new Thread(new Runnable() {
					public void run() {
						Rate discountRate = null;
						if (Db.getCreateTripResponse() != null) {
							discountRate = Db.getCreateTripResponse().getNewRate();
						}
						ConfirmationUtils.saveConfirmationData(mContext, Db.getSearchParams(),
								Db.getSelectedProperty(), Db.getSelectedRate(), Db.getBillingInfo(),
								Db.getBookingResponse(), discountRate);
					}
				}).start();
			}
		}

		// #13365: If the Db expired, finish out of this activity
		if (Db.getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		setContentView(R.layout.activity_confirmation_fragment);

		// We don't want to display the "succeeded with errors" dialog box if:
		// 1. It's not the first launch of the activity (savedInstanceState != null)
		// 2. We're re-launching the activity with saved confirmation data
		if (Db.getBookingResponse().succeededWithErrors() && savedInstanceState == null
				&& !ConfirmationUtils.hasSavedConfirmationData(this)) {
			showSucceededWithErrorsDialog();
		}

		// Track page load
		if (savedInstanceState == null) {
			Tracker.trackAppHotelsCheckoutConfirmation(this, Db.getSearchParams(), Db.getSelectedProperty(),
					Db.getBillingInfo(), Db.getSelectedRate(), Db.getBookingResponse());
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (isFinishing()) {
			Db.setBookingResponse(null);
			Db.setCouponDiscountRate(null);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// ActionBar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (AndroidUtils.isTablet(this)) {
			getSupportMenuInflater().inflate(R.menu.menu_fragment_standard, menu);
		}
		else {
			getSupportMenuInflater().inflate(R.menu.menu_confirmation, menu);
		}

		// Configure the ActionBar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setTitle(getString(R.string.booking_complete));
		actionBar.setDisplayUseLogoEnabled(!AndroidUtils.isTablet(this));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//This should never be reached as the actionbar app icon is disabled.
			return true;

		case R.id.menu_share:
			onShareBooking();
			return true;

		case R.id.menu_show_on_map:
			onShowOnMap();
			return true;

		case R.id.menu_new_search:
			onNewSearch();
			return true;

		case R.id.menu_about:
			Intent intent = new Intent(this, TabletAboutActivity.class);
			startActivity(intent);
			return true;

		}

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Breadcrumb (reloading activity)

	public boolean loadSavedConfirmationData() {
		Log.i("Loading saved confirmation data...");
		try {
			JSONObject data = new JSONObject(IoUtils.readStringFromFile(ConfirmationUtils.CONFIRMATION_DATA_FILE, this));
			Db.setSearchParams((SearchParams) JSONUtils.getJSONable(data, Codes.SEARCH_PARAMS, SearchParams.class));
			Db.setSelectedProperty((Property) JSONUtils.getJSONable(data, Codes.PROPERTY, Property.class));
			Db.setSelectedRate((Rate) JSONUtils.getJSONable(data, Codes.RATE, Rate.class));
			Db.setBillingInfo((BillingInfo) JSONUtils.getJSONable(data, Codes.BILLING_INFO, BillingInfo.class));
			Db.setBookingResponse((BookingResponse) JSONUtils.getJSONable(data, Codes.BOOKING_RESPONSE,
					BookingResponse.class));
			return true;
		}
		catch (Exception e) {
			Log.e("Could not load ConfirmationFragmentActivity state.", e);
			return false;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Actions

	public void showSucceededWithErrorsDialog() {
		FragmentManager fm = getSupportFragmentManager();
		String dialogTag = getString(R.string.tag_simple_dialog);
		if (fm.findFragmentByTag(dialogTag) == null) {
			String title = getString(R.string.error_booking_title);
			String message = getString(R.string.error_booking_succeeded_with_errors, Db.getBookingResponse()
					.gatherErrorMessage(this));

			SimpleSupportDialogFragment.newInstance(title, message).show(getSupportFragmentManager(), dialogTag);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// BookingConfirmationFragmentListener

	@Override
	public void onNewSearch() {
		Tracker.trackNewSearch(this);

		// Ensure we can't come back here again
		ConfirmationUtils.deleteSavedConfirmationData(mContext);
		Db.clear();

		Class<? extends Activity> routingTarget = ExpediaBookingApp.useTabletInterface(this)
				? SearchFragmentActivity.class
				: PhoneSearchActivity.class;

		Intent intent = new Intent(mContext, routingTarget);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Codes.EXTRA_NEW_SEARCH, true);

		startActivity(intent);
		finish();
	}

	@Override
	public void onShareBooking() {
		String contactText = ConfirmationUtils.determineContactText(this);
		ConfirmationUtils.share(this, Db.getSearchParams(), Db.getSelectedProperty(), Db.getBookingResponse(),
				Db.getBillingInfo(), Db.getSelectedRate(), Db.getCouponDiscountRate(), contactText);
	}

	@Override
	public void onShowOnMap() {
		Tracker.trackViewOnMap(this);
		startActivity(ConfirmationUtils.generateIntentToShowPropertyOnMap(Db.getSelectedProperty()));
	}

	//////////////////////////////////////////////////////////////////////////
	// MapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
