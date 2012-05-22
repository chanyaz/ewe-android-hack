package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.HotelItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.widget.BalloonItemizedOverlay;
import com.mobiata.android.widget.StandardBalloonAdapter;
import com.omniture.AppMeasurement;

public class HotelMapActivity extends MapActivity {

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

		// Configure header
		OnClickListener onBookNowClick = new OnClickListener() {
			public void onClick(View v) {
				Intent roomsRatesIntent = new Intent(mContext, RoomsAndRatesListActivity.class);
				startActivity(roomsRatesIntent);
			}
		};
		OnClickListener onReviewsClick = (!property.hasExpediaReviews()) ? null : new OnClickListener() {
			public void onClick(View v) {
				Intent userReviewsIntent = new Intent(mContext, UserReviewsListActivity.class);
				startActivity(userReviewsIntent);
			}
		};
		LayoutUtils.configureHeader(this, property, onBookNowClick, onReviewsClick);

		// Create the map and add it to the layout
		mMapView = MapUtils.createMapView(this);
		ViewGroup mapContainer = (ViewGroup) findViewById(R.id.map_layout);
		mapContainer.addView(mMapView);

		// Configure the map
		mMapView.setBuiltInZoomControls(true);
		mMapView.setSatellite(false);
		mMapView.setClickable(true);

		List<Property> properties = new ArrayList<Property>();
		properties.add(property);
		mOverlay = new HotelItemizedOverlay(this, properties, mMapView);
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
