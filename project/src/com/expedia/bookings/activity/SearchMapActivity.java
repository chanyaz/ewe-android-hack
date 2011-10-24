package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity.ExactSearchLocationSearchedListener;
import com.expedia.bookings.activity.SearchActivity.MapViewListener;
import com.expedia.bookings.activity.SearchActivity.SetShowDistanceListener;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.widget.HotelItemizedOverlay;
import com.expedia.bookings.widget.HotelItemizedOverlay.OnBalloonTap;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.widget.DoubleTapToZoomListenerOverlay;
import com.mobiata.android.widget.ExactLocationItemizedOverlay;

public class SearchMapActivity extends MapActivity implements SearchListener, OnFilterChangedListener, MapViewListener,
		SetShowDistanceListener, ExactSearchLocationSearchedListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private MapView mMapView;
	private SearchResponse mSearchResponse;
	private HotelItemizedOverlay mHotelItemizedOverlay;
	private ExactLocationItemizedOverlay mExactLocationItemizedOverlay;
	private DoubleTapToZoomListenerOverlay mDoubleTapToZoomListenerOverlay;

	private boolean mShowDistance = true;
	private double mExactLocationLatitude;
	private double mExactLocationLongitude;
	private String mExactLocationAddress;

	// save instance variables
	private static final String CURRENT_CENTER_LAT = "CURRENT_CENTER_LAT";
	private static final String CURRENT_CENTER_LON = "CURRENT_CENTER_LON";
	private static final String CURRENT_ZOOM_LEVEL = "CURRENT_ZOOM_LEVEL";
	private static final String CURRENT_TAPPED_ITEM_PROPERTY_ID = "CURRENT_TAPPED_ITEM_PROPERTY_ID";

	private static final String IS_EXACT_SEARCH_LOCATION_TAPPED = "IS_EXACT_LOCATION_TAPPED";
	private static final String EXACT_SEARCH_LOCATION_LAT = "EXACT_LOCATION_LAT";
	private static final String EXACT_SEARCH_LOCATION_LON = "EXACT_LOCATION_LON";
	private static final String EXACT_SEARCH_LOCATION_ADDRESS = "EXACT_LOCATION_ADDRESS";

	// saved information for map
	private GeoPoint mSavedCenter;
	private int mSavedZoomLevel;
	private String mTappedPropertyId;
	private boolean mIsExactLocationTapped;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_map);

		// Create the map and add it to the layout
		mMapView = MapUtils.createMapView(this);
		ViewGroup mapContainer = (ViewGroup) findViewById(R.id.map_layout);
		mapContainer.addView(mMapView);

		// Configure the map
		mMapView.setBuiltInZoomControls(true);
		mMapView.setSatellite(false);

		final SearchActivity parent = (SearchActivity) getParent();
		parent.addSearchListener(this);
		parent.setMapViewListener(this);
		parent.addSetShowDistanceListener(this);
		parent.addExactLocationSearchedListener(this);

		restoreMapState(savedInstanceState);

		restoreSavedExactSearchLocation();

		OnBalloonTap onTap = new OnBalloonTap() {
			@Override
			public void onBalloonTap(Property property) {
				final SearchActivity parent = (SearchActivity) getParent();

				Intent intent = new Intent(SearchMapActivity.this, HotelActivity.class);
				intent.putExtra(Codes.PROPERTY, property.toJson().toString());
				intent.putExtra(Codes.SEARCH_PARAMS, parent.getSearchParams().toString());
				intent.putExtra(Codes.SESSION, parent.getSession().toJson().toString());
				SearchMapActivity.this.startActivity(intent);
			}
		};
		mHotelItemizedOverlay = new HotelItemizedOverlay(this, null, true, mMapView, onTap);
		mHotelItemizedOverlay.setShowDistance(mShowDistance);
		mHotelItemizedOverlay.setThumbnailPlaceholder(R.drawable.ic_image_placeholder);

		mExactLocationItemizedOverlay = new ExactLocationItemizedOverlay(this, mMapView);
		mDoubleTapToZoomListenerOverlay = new DoubleTapToZoomListenerOverlay(this, mMapView);
	}

	@Override
	protected void onDestroy() {
		if (isFinishing() && mExactLocationAddress != null) {
			persistExactLocationToDisk();
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		focusOnProperties();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(CURRENT_CENTER_LAT, mMapView.getMapCenter().getLatitudeE6());
		outState.putInt(CURRENT_CENTER_LON, mMapView.getMapCenter().getLongitudeE6());
		outState.putInt(CURRENT_ZOOM_LEVEL, mMapView.getZoomLevel());

		if (mExactLocationAddress != null) {
			outState.putDouble(EXACT_SEARCH_LOCATION_LAT, mExactLocationLatitude);
			outState.putDouble(EXACT_SEARCH_LOCATION_LON, mExactLocationLongitude);
			outState.putString(EXACT_SEARCH_LOCATION_ADDRESS, mExactLocationAddress);

		}

		if (mExactLocationItemizedOverlay != null) {
			outState.putBoolean(IS_EXACT_SEARCH_LOCATION_TAPPED, mExactLocationItemizedOverlay.isExactLocationTapped());
		}

		String tappedPropertyId = (mHotelItemizedOverlay == null) ? null : mHotelItemizedOverlay.getTappedPropertyId();
		if (tappedPropertyId != null) {
			outState.putString(CURRENT_TAPPED_ITEM_PROPERTY_ID, tappedPropertyId);
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	//////////////////////////////////////////////////////////////////////////////////
	// SearchListener implementation

	@Override
	public void onSearchStarted() {
		clearResults();
	}

	@Override
	public void onSearchProgress(int strId) {
		// Do nothing.  SearchActivity should handle the display of search progress.
	}

	@Override
	public void onSearchFailed(String message) {
		// Do nothing.  SearchActivity should handle the display of search progress.
	}

	@Override
	public void onSearchCompleted(SearchResponse response) {
		if (response == null) {
			// We assume that the parent handles errors, but just in case, don't crash if this happens
			return;
		}

		mSearchResponse = response;
		mSearchResponse.getFilter().addOnFilterChangedListener(this);

		List<Overlay> overlays = mMapView.getOverlays();

		// Add hotels overlay
		Property[] propertyArray = mSearchResponse.getFilteredAndSortedProperties();
		List<Property> properties = new ArrayList<Property>();

		properties = propertyArray != null ? Arrays.asList(propertyArray) : null;
		// clear the map info to determine center based on new search
		clearSavedMapInfo();
		mHotelItemizedOverlay.setProperties(properties, mSearchResponse.getProperties());
		mHotelItemizedOverlay.setShowDistance(mShowDistance);

		mExactLocationItemizedOverlay.setExactLocation(mExactLocationLatitude, mExactLocationLongitude,
				mExactLocationAddress);
		if (mExactLocationAddress != null) {
			// restore the map to show the balloon for the
			// user specified location in the search
			if (mIsExactLocationTapped) {
				mExactLocationItemizedOverlay.showBalloon(0, false);
				mIsExactLocationTapped = false;
			}
		}

		if (!overlays.contains(mDoubleTapToZoomListenerOverlay)) {
			overlays.add(mDoubleTapToZoomListenerOverlay);
		}

		if (!overlays.contains(mHotelItemizedOverlay)) {
			overlays.add(mHotelItemizedOverlay);
		}

		if (!overlays.contains(mExactLocationItemizedOverlay)) {
			overlays.add(mExactLocationItemizedOverlay);
		}

		// Set the center point
		focusOnProperties();
	}

	@Override
	public void onSetShowDistance(boolean showDistance) {
		mShowDistance = showDistance;
		if (mHotelItemizedOverlay != null) {
			mHotelItemizedOverlay.setShowDistance(showDistance);
		}
	}

	@Override
	public boolean hasSearchResults() {
		return mSearchResponse != null && mSearchResponse.getPropertiesCount() > 0;
	}

	@Override
	public void clearResults() {
		mSearchResponse = null;
		mSavedCenter = null;
		mTappedPropertyId = null;
		mIsExactLocationTapped = false;

		mMapView.getOverlays().clear();

		if (mExactLocationItemizedOverlay != null) {
			mExactLocationItemizedOverlay.hideBalloon();
		}
	}

	@Override
	public void onFilterChanged() {
		mSavedCenter = mMapView.getMapCenter();
		mSavedZoomLevel = mMapView.getZoomLevel();
		mTappedPropertyId = mHotelItemizedOverlay.getTappedPropertyId();

		mHotelItemizedOverlay.setProperties(Arrays.asList(mSearchResponse.getFilteredAndSortedProperties()),
				mSearchResponse.getProperties());
		mMapView.invalidate();

		Boolean areHotelsVisible = mHotelItemizedOverlay.areHotelsVisible();

		/*
		 * Only restore the current map state across a filtering if there
		 * are hotels in the current visible area of the map
		 */
		if (areHotelsVisible != null && !areHotelsVisible.booleanValue()) {
			clearSavedMapInfo();
		}

		// Animate to a new center point
		focusOnProperties();
	}

	@Override
	public GeoPoint onRequestMapCenter() {
		if (mMapView != null) {
			return mMapView.getMapCenter();
		}

		return null;
	}

	public void focusOnProperties() {

		MapController mc = mMapView.getController();

		/*
		 * restore map state
		 */
		if (mSavedCenter != null) {
			mc.setCenter(mSavedCenter);
			mc.setZoom(mSavedZoomLevel);
			if (mTappedPropertyId != null) {
				mHotelItemizedOverlay.showBalloon(mTappedPropertyId);
			}
		}
		else {
			mc.animateTo(mHotelItemizedOverlay.getCenter());
			mc.zoomToSpan(mHotelItemizedOverlay.getLatSpanE6(), mHotelItemizedOverlay.getLonSpanE6());
			mSavedCenter = mHotelItemizedOverlay.getCenter();
			mSavedZoomLevel = mMapView.getZoomLevel();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// ExactSearchLocationSearchedListener implementation

	@Override
	public void onExactSearchLocationSpecified(double latitude, double longitude, String address) {
		mExactLocationLatitude = latitude;
		mExactLocationLongitude = longitude;
		mExactLocationAddress = address;
		if (mExactLocationItemizedOverlay != null) {
			mExactLocationItemizedOverlay.setExactLocation(mExactLocationLatitude, mExactLocationLongitude,
					mExactLocationAddress);
		}
		persistExactLocationToDisk();
	}

	@Override
	public void onNoExactSearchLocationSpecified() {
		onExactSearchLocationSpecified(0, 0, null);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Private methods

	private void restoreMapState(Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_ZOOM_LEVEL)) {
			int latE6 = savedInstanceState.getInt(CURRENT_CENTER_LAT);
			int lonE6 = savedInstanceState.getInt(CURRENT_CENTER_LON);

			mSavedCenter = new GeoPoint(latE6, lonE6);
			mSavedZoomLevel = savedInstanceState.getInt(CURRENT_ZOOM_LEVEL);

			mTappedPropertyId = savedInstanceState.getString(CURRENT_TAPPED_ITEM_PROPERTY_ID);

			mExactLocationAddress = savedInstanceState.getString(EXACT_SEARCH_LOCATION_ADDRESS);
			mExactLocationLatitude = savedInstanceState.getDouble(EXACT_SEARCH_LOCATION_LAT, 0.0);
			mExactLocationLatitude = savedInstanceState.getDouble(EXACT_SEARCH_LOCATION_LON, 0.0);
			mIsExactLocationTapped = savedInstanceState.getBoolean(IS_EXACT_SEARCH_LOCATION_TAPPED);
		}
	}

	private void clearSavedMapInfo() {
		mSavedCenter = null;
		mSavedZoomLevel = -1;
	}

	private void restoreSavedExactSearchLocation() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mExactLocationLatitude = prefs.getFloat(EXACT_SEARCH_LOCATION_LAT, 0.0f);
		mExactLocationLongitude = prefs.getFloat(EXACT_SEARCH_LOCATION_LON, 0.0f);
		mExactLocationAddress = prefs.getString(EXACT_SEARCH_LOCATION_ADDRESS, null);
	}

	private void persistExactLocationToDisk() {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			Editor editor = prefs.edit();
			if (mExactLocationAddress != null) {
				editor.putFloat(EXACT_SEARCH_LOCATION_LAT, (float) mExactLocationLatitude);
				editor.putFloat(EXACT_SEARCH_LOCATION_LON, (float) mExactLocationLongitude);
				editor.putString(EXACT_SEARCH_LOCATION_ADDRESS, mExactLocationAddress);
			}
			else {
				editor.remove(EXACT_SEARCH_LOCATION_LAT);
				editor.remove(EXACT_SEARCH_LOCATION_LON);
				editor.remove(EXACT_SEARCH_LOCATION_ADDRESS);
			}
			SettingUtils.commitOrApply(editor);
		}
		catch (OutOfMemoryError e) {
			Log.w("Ran out of memory while trying to save exact location", e);
		}
	}
}
