package com.expedia.bookings.activity;

import java.util.List;

import android.os.Bundle;

import com.expedia.bookings.R;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.widget.FixedMyLocationOverlay;
import com.mobiata.hotellib.widget.HotelItemizedOverlay;

public class SearchMapActivity extends MapActivity implements SearchListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private SearchActivity mParent;

	private MapView mMapView;

	private SearchResponse mSearchResponse;

	private MyLocationOverlay mMyLocationOverlay;

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
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mMyLocationOverlay != null) {
			mMyLocationOverlay.enableMyLocation();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mMyLocationOverlay != null) {
			mMyLocationOverlay.disableMyLocation();
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

		List<Overlay> overlays = mMapView.getOverlays();

		// Add hotels overlay
		HotelItemizedOverlay overlay = new HotelItemizedOverlay(this, mSearchResponse, mParent.getSearchParams(), true,
				mMapView, HotelActivity.class);
		overlays.add(overlay);

		// Add an overlay for my location
		if (mMyLocationOverlay == null) {
			mMyLocationOverlay = new FixedMyLocationOverlay(this, mMapView);
		}
		mMyLocationOverlay.enableMyLocation();
		overlays.add(mMyLocationOverlay);

		// Set the center point
		MapController mc = mMapView.getController();
		mc.animateTo(overlay.getCenter());
		mc.zoomToSpan(overlay.getLatSpanE6(), overlay.getLonSpanE6());
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

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

}
