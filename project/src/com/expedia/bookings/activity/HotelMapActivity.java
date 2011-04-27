package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import com.expedia.bookings.R;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.utils.JSONUtils;
import com.mobiata.hotellib.widget.HotelItemizedOverlay;
import com.omniture.AppMeasurement;

public class HotelMapActivity extends MapActivity {

	private Context mContext;

	private Property mProperty;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.activity_hotel_map);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY,
				Property.class);

		// TODO: Delete this once done testing
		// This code allows us to test the HotelActivity standalone, for layout purposes.
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
		LayoutUtils.configureHeader(this, mProperty, new OnClickListener() {
			public void onClick(View v) {
				Intent roomsRatesIntent = new Intent(mContext, RoomsAndRatesListActivity.class);
				roomsRatesIntent.fillIn(getIntent(), 0);
				startActivity(roomsRatesIntent);
			}
		});

		// Create the map and add it to the layout
		MapView mapView = MapUtils.createMapView(this);
		ViewGroup mapContainer = (ViewGroup) findViewById(R.id.map_layout);
		mapContainer.addView(mapView);

		// Configure the map
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(false);

		List<Property> properties = new ArrayList<Property>();
		properties.add(mProperty);
		HotelItemizedOverlay hotelOverlay = new HotelItemizedOverlay(this, properties, true, mapView, null);

		List<Overlay> overlays = mapView.getOverlays();
		overlays.add(hotelOverlay);

		MapController mc = mapView.getController();
		mc.setCenter(hotelOverlay.getCenter());
		mc.setZoom(15);

		hotelOverlay.onTap(0); // Open the popup initially

		if (getLastNonConfigurationInstance() == null) {
			onPageLoad();
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Just return something, so we know that all that happened was an orientation change
		return true;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	public void onPageLoad() {
		Log.d("Tracking \"App.Hotels.Infosite.Map\" event");

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
}
