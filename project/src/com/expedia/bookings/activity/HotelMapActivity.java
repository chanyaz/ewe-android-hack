package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
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
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.widget.BalloonItemizedOverlay;
import com.mobiata.android.widget.StandardBalloonAdapter;
import com.omniture.AppMeasurement;

public class HotelMapActivity extends MapActivity {

	private Context mContext;

	private Property mProperty;

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

		mContext = this;

		setContentView(R.layout.activity_hotel_map);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY,
				Property.class);

		// This code allows us to test the HotelMapActivity standalone, for layout purposes.
		// Just point the default launcher activity towards this instead of SearchActivity
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			try {
				mProperty = new Property();
				mProperty.fillWithTestData();
			}
			catch (JSONException e) {
				Log.e("Couldn't create dummy data!", e);
			}
		}

		// Configure header
		OnClickListener onBookNowClick = new OnClickListener() {
			public void onClick(View v) {
				Intent roomsRatesIntent = new Intent(mContext, RoomsAndRatesListActivity.class);
				roomsRatesIntent.fillIn(getIntent(), 0);
				startActivity(roomsRatesIntent);
			}
		};
		OnClickListener onReviewsClick = (!mProperty.hasExpediaReviews()) ? null : new OnClickListener() {
			public void onClick(View v) {
				Intent userReviewsIntent = new Intent(mContext, UserReviewsListActivity.class);
				userReviewsIntent.fillIn(getIntent(), 0);
				startActivity(userReviewsIntent);
			}
		};
		LayoutUtils.configureHeader(this, mProperty, onBookNowClick, onReviewsClick);

		// Create the map and add it to the layout
		mMapView = MapUtils.createMapView(this);
		ViewGroup mapContainer = (ViewGroup) findViewById(R.id.map_layout);
		mapContainer.addView(mMapView);

		// Configure the map
		mMapView.setBuiltInZoomControls(true);
		mMapView.setSatellite(false);
		mMapView.setClickable(true);

		List<Property> properties = new ArrayList<Property>();
		properties.add(mProperty);
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
		TrackingUtils.addProducts(s, mProperty);

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
