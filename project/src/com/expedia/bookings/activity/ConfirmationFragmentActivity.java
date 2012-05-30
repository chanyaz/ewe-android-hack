package com.expedia.bookings.activity;

import org.json.JSONObject;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentMapActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.BookingConfirmationFragment.BookingConfirmationFragmentListener;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleDialogFragment;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;

public class ConfirmationFragmentActivity extends FragmentMapActivity implements BookingConfirmationFragmentListener {

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

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
						ConfirmationUtils.saveConfirmationData(mContext, Db.getSearchParams(),
								Db.getSelectedProperty(), Db.getSelectedRate(), Db.getBillingInfo(),
								Db.getBookingResponse());
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
	protected void onStart() {
		super.onStart();

		// Configure the ActionBar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		if (AndroidUtils.getSdkVersion() >= 14) {
			actionBar.setHomeButtonEnabled(false);
		}
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_action_bar));
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (isFinishing()) {
			Db.setBookingResponse(null);
		}
	}

	@Override
	public void onBackPressed() {
		finish();
		Intent i = new Intent(this, SearchFragmentActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra(Codes.EXTRA_FINISH, true);
		startActivity(i);
	}

	//////////////////////////////////////////////////////////////////////////
	// ActionBar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_fragment_standard, menu);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayHomeAsUpEnabled(false);
		if (AndroidUtils.getSdkVersion() >= 14) {
			actionBar.setHomeButtonEnabled(false);
		}

		DebugMenu.onCreateOptionsMenu(this, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//This should never be reached as the actionbar app icon is disabled.
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

	public void newSearch() {
		Tracker.trackNewSearch(this);

		// Ensure we can't come back here again
		ConfirmationUtils.deleteSavedConfirmationData(mContext);

		Intent intent = new Intent(mContext, SearchFragmentActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(Codes.EXTRA_NEW_SEARCH, true);
		startActivity(intent);
		finish();
	}

	public void showSucceededWithErrorsDialog() {
		FragmentManager fm = getSupportFragmentManager();
		String dialogTag = getString(R.string.tag_simple_dialog);
		if (fm.findFragmentByTag(dialogTag) == null) {
			String title = getString(R.string.error_booking_title);
			String message = getString(R.string.error_booking_succeeded_with_errors, Db.getBookingResponse()
					.gatherErrorMessage(this));

			SimpleDialogFragment.newInstance(title, message).show(getFragmentManager(), dialogTag);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// BookingConfirmationFragmentListener

	@Override
	public void onNewSearch() {
		newSearch();
	}

	//////////////////////////////////////////////////////////////////////////
	// MapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
