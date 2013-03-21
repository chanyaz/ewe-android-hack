package com.expedia.bookings.fragment;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncAdapter;
import com.expedia.bookings.data.trips.Trip;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobiata.android.LocationServices;

public class ItineraryMapFragment extends SupportMapFragment implements OnMyLocationChangeListener {

	private static final float ZOOM_LEVEL = 13;

	private ItineraryMapFragmentListener mListener;

	private Map<Marker, ItinCardData> mMarkerToCard = new HashMap<Marker, ItinCardData>();

	private LatLngBounds mMarkerBounds;

	private String mSelectedId;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof ItineraryMapFragmentListener)) {
			throw new RuntimeException("ItineraryMapFragment Activity must implement listener");
		}

		mListener = (ItineraryMapFragmentListener) activity;

		ItineraryManager.getInstance().addSyncListener(mItinerarySyncAdapter);
	}

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
				if (mSelectedId == null) {
					mListener.onItinMarkerClicked(mMarkerToCard.get(marker));
				}

				return true;
			}
		});

		Activity activity = getActivity();
		if (activity instanceof OnCameraChangeListener) {
			map.setOnCameraChangeListener((OnCameraChangeListener) activity);
		}

		// Set the initial zoom level; otherwise all of our camera updates will be off target
		moveCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));

		showItinMarkers();
	}

	@Override
	public void onDetach() {
		super.onDetach();

		ItineraryManager.getInstance().removeSyncListener(mItinerarySyncAdapter);
	}

	private void showItinMarkers() {
		GoogleMap map = getMap();

		List<ItinCardData> data = ItineraryManager.getInstance().getItinCardData();
		boolean hasLocations = false;
		for (ItinCardData card : data) {
			if (card.getLocation() != null) {
				hasLocations = true;
				break;
			}
		}

		// Clear out all markers
		map.clear();
		mMarkerToCard.clear();

		if (hasLocations) {
			LatLngBounds.Builder builder = LatLngBounds.builder();

			for (ItinCardData card : data) {
				LatLng loc = card.getLocation();
				if (loc == null) {
					continue;
				}

				MarkerOptions opts = new MarkerOptions();
				opts.position(loc);
				opts.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_normal));
				Marker marker = map.addMarker(opts);

				mMarkerToCard.put(marker, card);

				builder.include(loc);
			}

			mMarkerBounds = builder.build();
		}
		else {
			mMarkerBounds = null;
		}
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
		GoogleMap map = getMap();

		if (mMarkerBounds != null) {
			map.setMyLocationEnabled(false);

			// Animate to show all markers on the map
			showBounds(mMarkerBounds, animate);
		}
		else {
			// Start animating to America regardless of what's going on
			showBounds(getAmericaBounds(), animate);

			if (LocationServices.areProvidersEnabled(getActivity())) {
				map.setMyLocationEnabled(true);
			}
			else {
				map.setMyLocationEnabled(false);
			}
		}
	}

	private void showBounds(LatLngBounds bounds, boolean animate) {
		changeCamera(CameraUpdateFactory.newLatLngBounds(bounds,
				(int) getResources().getDisplayMetrics().density * 50), animate);
	}

	public void hideItinItem() {
		mSelectedId = null;

		showFallback(true);
	}

	// Returns true if the camera position was changed
	public boolean showItinItem(ItinCardData data, boolean animate) {
		getMap().setMyLocationEnabled(false);

		mSelectedId = data.getId();

		LatLng position = null;

		if (data != null) {
			position = data.getLocation();
		}

		if (position == null) {
			position = new LatLng(0, 0);
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
	// ItinerarySyncAdapter

	private ItinerarySyncAdapter mItinerarySyncAdapter = new ItinerarySyncAdapter() {
		@Override
		public void onSyncFinished(Collection<Trip> trips) {
			showItinMarkers();

			if (mSelectedId == null) {
				showFallback(true);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// OnMyLocationChangeListener

	@Override
	public void onMyLocationChange(Location myLocation) {
		changeCamera(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), true, getCenterOffsetX(), 0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface ItineraryMapFragmentListener {
		public void onItinMarkerClicked(ItinCardData data);
	}

}
