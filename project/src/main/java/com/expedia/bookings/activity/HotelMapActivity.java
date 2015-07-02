package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.HotelMapFragment;
import com.expedia.bookings.fragment.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class HotelMapActivity extends FragmentActivity implements HotelMapFragmentListener {

	private HotelMapFragment mHotelMapFragment;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	//////////////////////////////////////////////////////////////////////////////////////////
	// Static Methods
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Create intent to open this activity in a standard way.
	 * @param context
	 * @return
	 */
	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, HotelMapActivity.class);
		return intent;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// LIFECYCLE EVENTS
	//----------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		// #13365: If the Db expired, finish out of this activity
		Property selectedProperty = Db.getHotelSearch().getSelectedProperty();
		if (selectedProperty == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		setContentView(R.layout.activity_hotel_map);

		mHotelMapFragment = Ui.findSupportFragment(this, getString(R.string.tag_single_hotel_map));
		mHotelMapFragment.setShowDistances(Db.getHotelSearch().getSearchParams().getSearchType().shouldShowDistance());
		mHotelMapFragment.setProperty(selectedProperty);
		mHotelMapFragment.focusProperty(selectedProperty, false, 12.0f);

		Location location = selectedProperty.getLocation();

		// Display the address
		TextView addressTextView = Ui.findView(this, R.id.address_text_view);
		addressTextView.setText(location.getStreetAddressString() + "\n" + location.toShortFormattedString());

		if (savedInstanceState == null) {
			OmnitureTracking.trackPageLoadHotelsInfositeMap(getApplicationContext());
		}
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
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.fade_in, R.anim.implode);
	}

	@TargetApi(11)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_hotel_details, menu);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		Property property = Db.getHotelSearch().getSelectedProperty();
		HotelUtils.setupActionBarHotelNameAndRating(this, property);

		final MenuItem select = menu.findItem(R.id.menu_select_hotel);
		HotelUtils.setupActionBarCheckmark(this, select, property.isAvailable());

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go back
			onBackPressed();
			return true;
		case R.id.menu_select_hotel:
			startActivity(HotelRoomsAndRatesActivity.createIntent(this));
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		mWasStopped = true;
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mWasStopped) {
			OmnitureTracking.trackPageLoadHotelsInfositeMap(getApplicationContext());
			mWasStopped = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// HotelMapFragmentListener

	@Override
	public void onHotelMapFragmentAttached(HotelMapFragment fragment) {
		//ignore
	}

	@Override
	public void onPropertyClicked(Property property) {
		mHotelMapFragment.showBalloon(property);
	}

	@Override
	public void onMapClicked() {
		//ignore
	}

	@Override
	public void onExactLocationClicked() {
		//ignore
	}

	@Override
	public void onPropertyBubbleClicked(Property property) {
		//ignore
	}
}
