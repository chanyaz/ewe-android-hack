package com.expedia.bookings.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobiata.android.LocationServices;

public class ItineraryMapFragment extends SupportMapFragment implements OnMyLocationChangeListener {

	private static final float ZOOM_LEVEL = 13;

	private Marker mMarker;

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		GoogleMap map = getMap();
		map.setOnMyLocationChangeListener(this);

		// At the moment, can't disable this via XML
		map.getUiSettings().setMyLocationButtonEnabled(false);

		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				// Consume all click events so users can't interact with markers
				return true;
			}
		});

		Activity activity = getActivity();
		if (activity instanceof OnCameraChangeListener) {
			map.setOnCameraChangeListener((OnCameraChangeListener) activity);
		}

		// Create an invisible marker that we will move around the screen
		// as itineraries are shown.
		MarkerOptions opts = new MarkerOptions();
		opts.position(new LatLng(0, 0));
		opts.visible(false);
		opts.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_normal));
		mMarker = map.addMarker(opts);

		// Set the initial zoom level; otherwise all of our camera updates will be off target
		moveCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
	}

	/**
	 * This is what should be displayed whenever no itinerary items are selected.
	 * 
	 * There are three options here:
	 * 
	 * 1. A map of all itineraries (if there are itineraries with locations)
	 * 2. The user's current location (if available)
	 * 3. America (if nothing else suffices)
	 */
	public void showFallback(boolean animate) {
		// TODO: Handle case where there are itins with locations

		// Start animating to America regardless of what's going on
		changeCamera(CameraUpdateFactory.newLatLngBounds(getAmericaBounds(),
				(int) getResources().getDisplayMetrics().density * 50), animate);

		GoogleMap map = getMap();
		if (LocationServices.areProvidersEnabled(getActivity())) {
			map.setMyLocationEnabled(true);
		}
		else {
			map.setMyLocationEnabled(false);
		}
	}

	public void hideItinItem() {
		mMarker.setVisible(false);

		showFallback(true);
	}

	// Returns true if the camera position was changed
	public boolean showItinItem(ItinCardData data, boolean animate) {
		getMap().setMyLocationEnabled(false);

		LatLng position = null;

		mMarker.setVisible(true);

		if (data != null) {
			position = data.getLocation();
		}

		if (position == null) {
			position = new LatLng(0, 0);
		}

		if (position.latitude == 0 && position.longitude == 0) {
			mMarker.setVisible(false);
		}
		else {
			mMarker.setPosition(position);
		}

		return changeCamera(position, animate);
	}

	private boolean changeCamera(LatLng target, boolean animate) {
		return changeCamera(target, animate, getCenterOffsetX(), getCenterOffsety());
	}

	/**
	 * Accounts for offset while moving camera 
	 * 
	 * @return true if camera position changed
	 */
	private boolean changeCamera(LatLng target, boolean animate, float offsetX, float offsetY) {
		CameraPosition origPosition = getMap().getCameraPosition();

		LatLng camLatLng = target;
		if (target.latitude != 0 || target.longitude != 0) {
			// Quickly set correct zoom level so we calculate offset correctly.  It's noticeable, but
			// only does anything if we're mid-animation anyways so it doesn't really matter.
			moveCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));

			camLatLng = offsetLatLng(camLatLng, offsetX, offsetY);
		}

		CameraPosition camPos = new CameraPosition(camLatLng, ZOOM_LEVEL, 0, 0);

		boolean camPosChanged = !practicallyEquals(origPosition, camPos);

		if (camPosChanged) {
			CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(camPos);
			if (animate) {
				animateCamera(camUpdate);
			}
			else {
				moveCamera(camUpdate);
			}
		}

		return camPosChanged;
	}

	//////////////////////////////////////////////////////////////////////////
	// OnMyLocationChangeListener

	@Override
	public void onMyLocationChange(Location myLocation) {
		changeCamera(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), true, getCenterOffsetX(), 0);
	}
}
