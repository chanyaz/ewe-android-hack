package com.expedia.bookings.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.HotelMapFragment;
import com.expedia.bookings.fragment.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.util.ViewUtils;
import com.omniture.AppMeasurement;

public class HotelMapActivity extends SherlockFragmentMapActivity implements HotelMapFragmentListener {

	private HotelMapFragment mHotelMapFragment;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		// #13365: If the Db expired, finish out of this activity
		if (Db.getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		setContentView(R.layout.activity_hotel_map);

		mHotelMapFragment = Ui.findSupportFragment(this, getString(R.string.tag_single_hotel_map));
		mHotelMapFragment.setShowSingleProperty(true);
		mHotelMapFragment.notifySearchLocationFound();

		Property property = Db.getSelectedProperty();
		Location location = property.getLocation();

		// Display the address
		TextView addressTextView = Ui.findView(this, R.id.address_text_view);
		addressTextView.setText(location.getStreetAddressString() + "\n" + location.toFormattedString());

		if (savedInstanceState == null) {
			onPageLoad();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.fade_in, R.anim.implode);
	}

	@TargetApi(11)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_hotel_details, menu);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		ViewGroup titleView = (ViewGroup) getLayoutInflater().inflate(R.layout.actionbar_hotel_name_with_stars, null);

		Property property = Db.getSelectedProperty();
		String title = property.getName();
		((TextView) titleView.findViewById(R.id.title)).setText(title);

		float rating = (float) property.getHotelRating();
		((RatingBar) titleView.findViewById(R.id.rating)).setRating(rating);

		actionBar.setCustomView(titleView);

		final MenuItem select = menu.findItem(R.id.menu_select_hotel);
		Button tv = (Button) getLayoutInflater().inflate(R.layout.actionbar_select_hotel, null);
		ViewUtils.setAllCaps(tv);
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected(select);
			}
		});
		select.setActionView(tv);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go back
			Intent intent = new Intent(this, HotelDetailsFragmentActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return true;
		case R.id.menu_select_hotel:
			Intent roomsRatesIntent = new Intent(this, RoomsAndRatesListActivity.class);
			startActivity(roomsRatesIntent);
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
			onPageLoad();
			mWasStopped = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mKillReceiver.onDestroy();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	public void onPageLoad() {
		Log.d("Tracking \"App.Hotels.Infosite.Map\" pageLoad");

		AppMeasurement s = new AppMeasurement(getApplication());

		TrackingUtils.addStandardFields(this, s);

		s.pageName = "App.Hotels.Infosite.Map";

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Shopper";

		// Products
		TrackingUtils.addProducts(s, Db.getSelectedProperty());

		// Send the tracking data
		s.track();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// HotelMapFragmentListener

	@Override
	public void onBalloonShown(Property property) {
	}

	@Override
	public void onBalloonClicked(Property property) {
	}

}
