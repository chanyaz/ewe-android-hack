package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity.MapViewListener;
import com.expedia.bookings.activity.SearchActivity.SetShowDistanceListener;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.mobiata.android.MapUtils;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Filter.OnFilterChangedListener;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.widget.FixedMyLocationOverlay;
import com.mobiata.hotellib.widget.HotelItemizedOverlay;
import com.mobiata.hotellib.widget.HotelItemizedOverlay.OnBalloonTap;

public class SearchMapActivity extends MapActivity implements SearchListener, OnFilterChangedListener, MapViewListener,
		SetShowDistanceListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private MapView mMapView;
	private SearchResponse mSearchResponse;
	private HotelItemizedOverlay mHotelItemizedOverlay;
	private MyLocationOverlay mMyLocationOverlay;

	// Keeps track of whether this Activity is being actively displayed.  If not, do not
	// enable the MyLocationOverlay.
	private boolean mIsActive;
	private boolean mShowDistance = true;
	
	// save instance variables
	private static final String CURRENT_CENTER_LAT = "CURRENT_CENTER_LAT";
	private static final String CURRENT_CENTER_LON = "CURRENT_CENTER_LON";
	private static final String CURRENT_ZOOM_LEVEL = "CURRENT_ZOOM_LEVEL";
	private static final String CURRENT_TAPPED_ITEM_PROPERTY_ID = "CURRENT_TAPPED_ITEM_PROPERTY_ID";
	
	// saved information for map
	private GeoPoint mSavedCenter;
	private int mSavedZoomLevel;
	private String mTappedPropertyId;
	
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

		restoreMapState(savedInstanceState);

		ActivityState state = (ActivityState) getLastNonConfigurationInstance();
		if (state != null) {
			mSearchResponse = state.searchResponse;

			if (mSearchResponse != null) {
				onSearchCompleted(mSearchResponse);
			}
		}
		else {

		}

		mIsActive = false;
		
	}

	@Override
	protected void onPause() {
		super.onPause();

		mIsActive = false;

		if (mMyLocationOverlay != null) {
			mMyLocationOverlay.disableMyLocation();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mIsActive = true;

		if (mMyLocationOverlay != null) {
			mMyLocationOverlay.enableMyLocation();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(CURRENT_CENTER_LAT, mMapView.getMapCenter().getLatitudeE6());
		outState.putInt(CURRENT_CENTER_LON, mMapView.getMapCenter().getLongitudeE6());
		outState.putInt(CURRENT_ZOOM_LEVEL, mMapView.getZoomLevel());
		
		String tappedPropertyId = mHotelItemizedOverlay.getTappedPropertyId();
		if(tappedPropertyId != null) {
			outState.putString(CURRENT_TAPPED_ITEM_PROPERTY_ID, tappedPropertyId);
		}
		
		super.onSaveInstanceState(outState);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		ActivityState state = new ActivityState();
		state.searchResponse = mSearchResponse;

		return state;
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
		if (mHotelItemizedOverlay == null) {
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
			mHotelItemizedOverlay = new HotelItemizedOverlay(this, properties, true, mMapView, onTap);
			mHotelItemizedOverlay.setShowDistance(mShowDistance);
			mHotelItemizedOverlay.setThumbnailPlaceholder(R.drawable.ic_image_placeholder);
			overlays.add(mHotelItemizedOverlay);
		}
		else {
			// clear the map info to determine center based on new search
			clearSavedMapInfo();
			mHotelItemizedOverlay.setProperties(properties, mSearchResponse.getProperties());
			mHotelItemizedOverlay.setShowDistance(mShowDistance);
		}

		// Add an overlay for my location
		if (mMyLocationOverlay == null) {
			mMyLocationOverlay = new FixedMyLocationOverlay(this, mMapView);
			overlays.add(mMyLocationOverlay);
		}
		if (mIsActive) {
			mMyLocationOverlay.enableMyLocation();
		}

		// Set the center point
		focusOnProperties();
	}

	@Override
	public void onSetShowDistance(boolean showDistance) {
		mShowDistance = showDistance;
		if(mHotelItemizedOverlay != null) {
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

		mMapView.getOverlays().clear();

		if (mMyLocationOverlay != null) {
			mMyLocationOverlay.disableMyLocation();
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
		if(areHotelsVisible != null && !areHotelsVisible.booleanValue()) {
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
		if(mSavedCenter != null) {
			mc.setCenter(mSavedCenter);
			mc.setZoom(mSavedZoomLevel);
			if(mTappedPropertyId != null) {
				mHotelItemizedOverlay.showBalloon(mTappedPropertyId);
			}
		} else {
			mc.animateTo(mHotelItemizedOverlay.getCenter());
			mc.zoomToSpan(mHotelItemizedOverlay.getLatSpanE6(), mHotelItemizedOverlay.getLonSpanE6());
			mSavedCenter = mHotelItemizedOverlay.getCenter();
			mSavedZoomLevel = mMapView.getZoomLevel();
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// Private methods
	
	private void restoreMapState(Bundle savedInstanceState) {
		if(savedInstanceState != null && savedInstanceState.containsKey(CURRENT_ZOOM_LEVEL)) {
			int latE6 = savedInstanceState.getInt(CURRENT_CENTER_LAT);
			int lonE6 = savedInstanceState.getInt(CURRENT_CENTER_LON);
			
			mSavedCenter = new GeoPoint(latE6, lonE6);
			mSavedZoomLevel = savedInstanceState.getInt(CURRENT_ZOOM_LEVEL);
			
			mTappedPropertyId = savedInstanceState.getString(CURRENT_TAPPED_ITEM_PROPERTY_ID);
		}
	}
	
	private void clearSavedMapInfo() {
		mSavedCenter = null;
		mSavedZoomLevel = -1;
	}
	

	
	//////////////////////////////////////////////////////////////////////////////////////////
	// Private classes

	private class ActivityState {
		public SearchResponse searchResponse;
	}
}
