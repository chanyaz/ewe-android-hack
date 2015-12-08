package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncAdapter;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.utils.GoogleMapsUtil;
import com.expedia.bookings.utils.Ui;
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

public class ItinMapFragment extends SupportMapFragment implements OnMyLocationChangeListener {

	private static final float ZOOM_LEVEL = 13;

	// The min/max longitude span for the fallback bounds.  This keeps things from being
	// too zoomed in (or too zoomed out).
	private static final double MIN_LON_SPAN = 2;
	private static final double MAX_LON_SPAN = 120;

	private ItineraryMapFragmentListener mListener;

	private Map<Marker, ItinCardData> mMarkerToCard = new HashMap<Marker, ItinCardData>();

	// The bounds we display when we fallback
	private LatLngBounds mMarkerBounds;

	private String mSelectedId;

	private int mListWidth;
	private int mMarkerSpacing;
	private int mItinMapBoundsPadding;

	// Invisible Fragment that handles FusedLocationProvider
	private FusedLocationProviderFragment mLocationFragment;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		mListener = Ui.findFragmentListener(this, ItineraryMapFragmentListener.class);

		mLocationFragment = FusedLocationProviderFragment.getInstance(this);

		ItineraryManager.getInstance().addSyncListener(mItinerarySyncAdapter);
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mItinMapBoundsPadding = getResources().getDimensionPixelSize(R.dimen.itin_map_bounds_padding);

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

		// It's not at all clear to me why, but if I leave this out, the initial zoom doesn't work.  Go figure.
		moveCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));

		showItinMarkers();
	}

	@Override
	public void onDetach() {
		super.onDetach();

		ItineraryManager.getInstance().removeSyncListener(mItinerarySyncAdapter);
	}

	public void setListWidth(int listWidth) {
		mListWidth = listWidth;
	}

	public void setMarkerSpacing(int space) {
		mMarkerSpacing = space;
	}

	private void showItinMarkers() {
		GoogleMap map = getMap();

		List<ItinCardData> data = new ArrayList<ItinCardData>();
		data.addAll(ItineraryManager.getInstance().getItinCardData());
		boolean hasLocations = false;
		for (ItinCardData card : data) {
			if (isValidLatLng(card.getLocation())) {
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
				if (!isValidLatLng(loc)) {
					continue;
				}

				MarkerOptions opts = new MarkerOptions();
				opts.position(loc);
				opts.icon(BitmapDescriptorFactory.fromResource(Ui.obtainThemeResID(getActivity(), R.attr.skin_hotelListMapMarkerDrawable)));
				Marker marker = map.addMarker(opts);

				mMarkerToCard.put(marker, card);

				builder.include(loc);

				// Increase bounds only if the lonspan is not too great
				LatLngBounds currBounds = builder.build();
				if (getLonSpan(currBounds) < MAX_LON_SPAN) {
					mMarkerBounds = currBounds;
				}
			}

			// This can easily happen if we only have on itin card (or they are all close to each other)
			if (getLonSpan(mMarkerBounds) < MIN_LON_SPAN) {
				double adjust = MIN_LON_SPAN / 2;
				mMarkerBounds = new LatLngBounds(
						new LatLng(mMarkerBounds.southwest.latitude - adjust,
								mMarkerBounds.southwest.longitude - adjust),
						new LatLng(
								mMarkerBounds.northeast.latitude + adjust,
								mMarkerBounds.northeast.longitude + adjust));
			}
		}
		else {
			mMarkerBounds = null;
		}
	}

	private static double getLonSpan(LatLngBounds bounds) {
		double span = bounds.northeast.longitude - bounds.southwest.longitude;

		if (span < 0) {
			// spans int'l date line
			span = (bounds.northeast.longitude + 180) + (180 - bounds.southwest.longitude);
		}

		return span;
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
			GoogleMapsUtil.setMyLocationEnabled(getActivity(), map, false);

			// Animate to show all markers on the map (or some markers, if the span was too large)
			showBounds(mMarkerBounds, animate);
		}
		else {
			// Start animating to America regardless of what's going on
			showBounds(getAmericaBounds(), animate);

			if (mLocationFragment.isLocationEnabled()) {
				GoogleMapsUtil.setMyLocationEnabled(getActivity(), map, true);
			}
			else {
				GoogleMapsUtil.setMyLocationEnabled(getActivity(), map, false);
			}
		}
	}

	private void showBounds(LatLngBounds bounds, boolean animate) {
		setPadding(mListWidth, 0, 0, 0);
		changeCamera(CameraUpdateFactory.newLatLngBounds(bounds, mItinMapBoundsPadding), animate);
	}

	public void hideItinItem() {
		mSelectedId = null;

		showFallback(true);
	}

	// Returns true if the camera position was changed
	public boolean showItinItem(ItinCardData data, boolean animate) {
		setPadding(mListWidth, getHeight() - mMarkerSpacing, 0, 0);
		GoogleMapsUtil.setMyLocationEnabled(getActivity(), getMap(), false);
		String id = data.getId();
		if (id.equals(mSelectedId)) {
			return false;
		}

		LatLng position = data.getLocation();

		if (position == null) {
			return false;
		}

		mSelectedId = id;

		CameraPosition newPos = new CameraPosition(position, ZOOM_LEVEL, 0, 0);
		CameraPosition origPos = getMap().getCameraPosition();

		if (practicallyEquals(origPos, newPos)) {
			return false;
		}

		changeCamera(position, animate);

		return true;
	}

	private void changeCamera(LatLng target, boolean animate) {
		changeCamera(CameraUpdateFactory.newLatLngZoom(target, ZOOM_LEVEL), animate);
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
		// This is only on as a fallback like when there are no itins to show
		setPadding(mListWidth, 0, 0, 0);
		changeCamera(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), true);
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface ItineraryMapFragmentListener {
		void onItinMarkerClicked(ItinCardData data);
	}
}
