package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
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
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.widget.HotelItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.android.widget.BalloonItemizedOverlay;
import com.mobiata.android.widget.StandardBalloonAdapter;
import com.omniture.AppMeasurement;

public class HotelMapActivity extends SherlockFragmentMapActivity {

	private Context mContext;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	// save instance variables
	private static final String CURRENT_ZOOM_LEVEL = "CURRENT_ZOOM_LEVEL";

	// saved information for map
	private int mSavedZoomLevel;
	private MapView mMapView;
	private HotelItemizedOverlay mOverlay;

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

		setContentView(R.layout.activity_hotel_map);

		Property property = Db.getSelectedProperty();
		Location location = property.getLocation();

		// Create the map and add it to the layout
		mMapView = MapUtils.createMapView(this);
		ViewGroup mapContainer = (ViewGroup) findViewById(R.id.map_layout);
		mapContainer.addView(mMapView);
		
		// Display the address
		TextView addressTextView = Ui.findView(this, R.id.address_text_view);
		addressTextView.setText(location.getStreetAddressString() + "\n" + location.toFormattedString());

		// Configure the map
		mMapView.setBuiltInZoomControls(true);
		mMapView.setSatellite(false);
		mMapView.setClickable(true);

		List<Property> properties = new ArrayList<Property>();
		properties.add(property);
		mOverlay = new HotelItemizedOverlay(this, properties, mMapView);
		mOverlay.setBalloonDrawable(R.drawable.bg_map_balloon);
		mOverlay.setClickable(false);
		StandardBalloonAdapter adapter = new StandardBalloonAdapter(this);
		adapter.setThumbnailPlaceholderResource(R.drawable.ic_image_placeholder);
		adapter.setShowChevron(false);
		mOverlay.setBalloonAdapter(adapter);

		List<Overlay> overlays = mMapView.getOverlays();
		overlays.add(mOverlay);

		// Set the center point
		if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_ZOOM_LEVEL)) {
			restoreMapState(savedInstanceState);
		}
		else {
			MapController mc = mMapView.getController();
			mc.setZoom(16);
			mc.setCenter(mOverlay.getCenter());
		}

		mOverlay.showBalloon(0, BalloonItemizedOverlay.F_FOCUS + BalloonItemizedOverlay.F_OFFSET_MARKER); // Open the popup initially

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
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(CURRENT_ZOOM_LEVEL, mMapView.getZoomLevel());
		super.onSaveInstanceState(outState);
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

	private void restoreMapState(Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_ZOOM_LEVEL)) {
			mSavedZoomLevel = savedInstanceState.getInt(CURRENT_ZOOM_LEVEL);
			mMapView.getController().setZoom(mSavedZoomLevel);
		}
	}

}
