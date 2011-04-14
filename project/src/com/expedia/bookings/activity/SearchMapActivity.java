package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.expedia.bookings.R;
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
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchParams.SearchType;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.widget.FixedMyLocationOverlay;
import com.mobiata.hotellib.widget.HotelItemizedOverlay;
import com.mobiata.hotellib.widget.HotelItemizedOverlay.OnBalloonTap;

public class SearchMapActivity extends MapActivity implements SearchListener, OnFilterChangedListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private Context mContext;
	private SearchActivity mParent;
	private MapView mMapView;
	private SearchResponse mSearchResponse;
	private HotelItemizedOverlay mHotelItemizedOverlay;
	private MyLocationOverlay mMyLocationOverlay;

	private ImageButton mMapSearchButton;

	// Keeps track of whether this Activity is being actively displayed.  If not, do not
	// enable the MyLocationOverlay.
	private boolean mIsActive;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_map);

		mContext = this;

		// Configure the map
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setSatellite(false);

		mMapSearchButton = (ImageButton) findViewById(R.id.map_search_button);
		mMapSearchButton.setOnClickListener(mMapSearchButtonClickListener);

		mParent = (SearchActivity) getParent();
		mParent.addSearchListener(this);

		ActivityState state = (ActivityState) getLastNonConfigurationInstance();
		if (state != null) {
			mSearchResponse = state.searchResponse;
			//mHotelItemizedOverlay = state.hotelItemizedOverlay;
			//mMyLocationOverlay = state.myLocationOverlay;

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

		if (mSearchResponse != null) {
			mSearchResponse.getFilter().removeOnFilterChangedListener(this);
		}
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
	public Object onRetainNonConfigurationInstance() {
		ActivityState state = new ActivityState();
		state.searchResponse = mSearchResponse;
		state.hotelItemizedOverlay = mHotelItemizedOverlay;
		state.myLocationOverlay = mMyLocationOverlay;

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
			// TODO: Error handling?  Or should we assume that the parent never calls this with null?
			return;
		}

		mSearchResponse = response;

		response.getFilter().addOnFilterChangedListener(this);

		List<Overlay> overlays = mMapView.getOverlays();

		// Add hotels overlay
		Property[] propertyArray = mSearchResponse.getFilteredAndSortedProperties();
		List<Property> properties = new ArrayList<Property>();

		properties = propertyArray != null ? Arrays.asList(propertyArray) : null;
		if (mHotelItemizedOverlay == null) {
			OnBalloonTap onTap = new OnBalloonTap() {
				@Override
				public void onBalloonTap(Property property) {
					Intent intent = new Intent(mContext, HotelActivity.class);
					intent.putExtra(Codes.PROPERTY, property.toJson().toString());
					intent.putExtra(Codes.SEARCH_PARAMS, mParent.getSearchParams().toString());
					intent.putExtra(Codes.SESSION, mParent.getSession().toJson().toString());
					mContext.startActivity(intent);
				}
			};
			mHotelItemizedOverlay = new HotelItemizedOverlay(this, properties, true, mMapView, onTap);
			mHotelItemizedOverlay.setThumbnailPlaceholder(R.drawable.ic_image_placeholder);
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
		mHotelItemizedOverlay.setProperties(Arrays.asList(mSearchResponse.getFilteredAndSortedProperties()));

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

	//////////////////////////////////////////////////////////////////////////////////
	// Listeners

	private final View.OnClickListener mMapSearchButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			GeoPoint center = mMapView.getMapCenter();
			SearchParams searchParams = mParent.getSearchParams();

			searchParams.setSearchType(SearchType.PROXIMITY);

			mParent.setSearchParams(searchParams);
			mParent.setSearchParams(MapUtils.getLatitiude(center), MapUtils.getLongitiude(center));
			mParent.startSearch();
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// Private classes

	private class ActivityState {
		public SearchResponse searchResponse;
		public HotelItemizedOverlay hotelItemizedOverlay;
		public MyLocationOverlay myLocationOverlay;
	}
}
