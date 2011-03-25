package com.expedia.bookings.activity;

import java.util.Arrays;
import java.util.List;

import android.os.Bundle;

import com.expedia.bookings.R;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Filter.OnFilterChangedListener;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.widget.FixedMyLocationOverlay;
import com.mobiata.hotellib.widget.HotelItemizedOverlay;

public class SearchMapActivity extends MapActivity implements SearchListener, OnFilterChangedListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private SearchActivity mParent;

	private MapView mMapView;

	private SearchResponse mSearchResponse;

	private HotelItemizedOverlay mHotelItemizedOverlay;

	private MyLocationOverlay mMyLocationOverlay;

	// Keeps track of whether this Activity is being actively displayed.  If not, do not
	// enable the MyLocationOverlay.
	private boolean mIsActive;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_map);

		// Configure the map
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setSatellite(false);

		mParent = (SearchActivity) getParent();
		mParent.addSearchListener(this);

		mIsActive = false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		mIsActive = true;

		if (mMyLocationOverlay != null) {
			mMyLocationOverlay.enableMyLocation();
		}

		if (mSearchResponse != null) {
			mSearchResponse.getFilter().addOnFilterChangedListener(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		mIsActive = false;

		if (mMyLocationOverlay != null) {
			mMyLocationOverlay.disableMyLocation();
		}

		if (mSearchResponse != null) {
			mSearchResponse.getFilter().removeOnFilterChangedListener(this);
		}
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
			// TODO: Error handling?  Or should we assume that the parent never calls this with null?
			return;
		}

		mSearchResponse = response;

		response.getFilter().addOnFilterChangedListener(this);

		List<Overlay> overlays = mMapView.getOverlays();

		// Add hotels overlay
		List<Property> properties = Arrays.asList(mSearchResponse
				.getFilteredAndSortedProperties());
		if (mHotelItemizedOverlay == null) {
			mHotelItemizedOverlay = new HotelItemizedOverlay(this, properties,
					mParent.getSearchParams(), true,
					mMapView, HotelActivity.class);
			overlays.add(mHotelItemizedOverlay);
		}
		else {
			mHotelItemizedOverlay.setProperties(properties);
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
		mHotelItemizedOverlay.setProperties(Arrays.asList(mSearchResponse
				.getFilteredAndSortedProperties()));

		// Animate to a new center point
		focusOnProperties();
	}

	public void focusOnProperties() {
		MapController mc = mMapView.getController();
		mc.animateTo(mHotelItemizedOverlay.getCenter());
		mc.zoomToSpan(mHotelItemizedOverlay.getLatSpanE6(), mHotelItemizedOverlay.getLonSpanE6());
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

}
