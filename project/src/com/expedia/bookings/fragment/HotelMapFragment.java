package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.widget.HotelItemizedOverlay;
import com.expedia.bookings.widget.SimpleBalloonAdapter;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.widget.BalloonItemizedOverlay;
import com.mobiata.android.widget.BalloonItemizedOverlay.BalloonListener;
import com.mobiata.android.widget.DoubleTapToZoomListenerOverlay;
import com.mobiata.android.widget.ExactLocationItemizedOverlay;
import com.mobiata.android.widget.StandardBalloonAdapter;

public class HotelMapFragment extends Fragment {

	public static HotelMapFragment newInstance() {
		return new HotelMapFragment();
	}

	//////////////////////////////////////////////////////////////////////////
	// Member variables

	private static final String INSTANCE_SHOW_DISTANCES = "INSTANCE_SHOW_DISTANCES";
	private static final String INSTANCE_SHOW_SINGLE_PROPERTY = "INSTANCE_SHOW_SINGLE_PROPERTY";
	private static final String INSTANCE_ZOOM_LEVEL = "INSTANCE_ZOOM_LEVEL";
	private static final String INSTANCE_LAT_LNG = "INSTANCE_LAT_LNG";

	private HotelMapFragmentListener mListener;

	private MapView mMapView;
	private HotelItemizedOverlay mHotelOverlay;
	private DoubleTapToZoomListenerOverlay mDoubleTapToZoomOverlay;
	private ExactLocationItemizedOverlay mExactLocationOverlay;

	private boolean mShowDistances = false;
	private boolean mShowSingleProperty = false;

	// We need to save these explicitly because if this fragment is used
	// more than once (as we do in PhoneSearchActivity and HotelMapActivity),
	// the map will inherit the last used values.
	private int mSavedZoomLevel = -1;
	private GeoPoint mSavedMapCenter = null;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mShowDistances = savedInstanceState.getBoolean(INSTANCE_SHOW_DISTANCES);
			mShowSingleProperty = savedInstanceState.getBoolean(INSTANCE_SHOW_SINGLE_PROPERTY);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof HotelMapFragmentListener)) {
			throw new RuntimeException("HotelMapFragment Activity must implement HotelMapFragmentListener!");
		}

		mListener = (HotelMapFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Activity activity = getActivity();

		LinearLayout mapLayout = new LinearLayout(activity);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mapLayout.setLayoutParams(params);

		mMapView = MapUtils.createMapView(activity);
		mMapView.setClickable(true);
		mapLayout.addView(mMapView);

		// Restore the mapview's location and zoom
		if (savedInstanceState != null) {
			mSavedZoomLevel = savedInstanceState.getInt(INSTANCE_ZOOM_LEVEL);
			int[] latlng = savedInstanceState.getIntArray(INSTANCE_LAT_LNG);
			mSavedMapCenter = new GeoPoint(latlng[0], latlng[1]);
		}

		// Add the initial overlays
		List<Overlay> overlays = mMapView.getOverlays();

		mExactLocationOverlay = new ExactLocationItemizedOverlay(activity, mMapView);
		mExactLocationOverlay.setBalloonAdapter(new SimpleBalloonAdapter(getActivity()));
		overlays.add(mExactLocationOverlay);

		mHotelOverlay = new HotelItemizedOverlay(activity, null, mMapView);
		mHotelOverlay.setBalloonDrawable(R.drawable.bg_map_balloon);

		StandardBalloonAdapter adapter = new StandardBalloonAdapter(getActivity());
		adapter.setThumbnailPlaceholderResource(R.drawable.ic_image_placeholder);
		adapter.setShowChevron(false);
		mHotelOverlay.setBalloonListener(new BalloonListener() {
			@Override
			public void onBalloonShown(int index) {
				mListener.onBalloonShown(mHotelOverlay.getProperty(index));
			}

			@Override
			public void onBalloonClicked(int index) {
				mListener.onBalloonClicked(mHotelOverlay.getProperty(index));
			}

			@Override
			public void onBalloonHidden() {
				// Do nothing
			}
		});
		mHotelOverlay.setBalloonAdapter(adapter);

		mHotelOverlay.setCenterOffset(0, getResources().getDimensionPixelSize(R.dimen.center_vertical_offset));
		overlays.add(mHotelOverlay);

		mDoubleTapToZoomOverlay = new DoubleTapToZoomListenerOverlay(activity, mMapView);
		overlays.add(mDoubleTapToZoomOverlay);

		return mapLayout;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateView();
		selectBalloonForProperty();

		if (mSavedZoomLevel != -1 && mSavedMapCenter != null) {
			MapController mc = mMapView.getController();
			mc.setZoom(mSavedZoomLevel);
			mc.setCenter(mSavedMapCenter);
		}
	}

	public void onPause() {
		super.onPause();
		mSavedZoomLevel = mMapView.getZoomLevel();
		mSavedMapCenter = mMapView.getMapCenter();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(INSTANCE_SHOW_DISTANCES, mShowDistances);
		outState.putBoolean(INSTANCE_SHOW_SINGLE_PROPERTY, mShowSingleProperty);

		mSavedZoomLevel = mMapView.getZoomLevel();
		outState.putInt(INSTANCE_ZOOM_LEVEL, mSavedZoomLevel);

		mSavedMapCenter = mMapView.getMapCenter();
		int[] latlng = { mSavedMapCenter.getLatitudeE6(), mSavedMapCenter.getLongitudeE6() };
		outState.putIntArray(INSTANCE_LAT_LNG, latlng);
	}

	@Override
	public void onDestroyView() {
		// remove the map view from the container so that its
		// view is not destroyed by the os to enable re-use
		((ViewGroup) mMapView.getParent()).removeAllViews();
		mMapView.getOverlays().clear();
		mHotelOverlay.destroyBalloon();
		mExactLocationOverlay.destroyBalloon();

		mHotelOverlay = null;
		mExactLocationOverlay = null;
		mDoubleTapToZoomOverlay = null;

		super.onDestroyView();
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	public void setShowDistances(boolean showDistances) {
		mShowDistances = showDistances;
		updateView();
	}

	public void setShowSingleProperty(boolean showSingleProperty) {
		mShowSingleProperty = showSingleProperty;
	}

	public void notifySearchStarted() {
		if (mHotelOverlay != null) {
			mHotelOverlay.setProperties(null);
		}
	}

	public void notifySearchLocationFound() {
		animateToSearchLocation();
	}

	public void notifySearchComplete() {
		updateView();
		showAllResults();
	}

	public void notifyPropertySelected() {
		selectBalloonForProperty();
	}

	public void notifyFilterChanged() {
		updateView();
	}

	public GeoPoint getCenter() {
		return mMapView.getMapCenter();
	}

	private void updateView() {
		// only update the view if the map view exists
		// and if there are overlay items to show on the map
		if (mHotelOverlay != null && mMapView != null) {
			if (mShowSingleProperty) {
				Property property = Db.getSelectedProperty();
				if (property != null) {
					mHotelOverlay.setSingleProperty(property);
					mHotelOverlay.setShowDistance(mShowDistances);
					mMapView.invalidate();
					mHotelOverlay.showBalloon(0, BalloonItemizedOverlay.F_FOCUS
							+ BalloonItemizedOverlay.F_OFFSET_MARKER); // Open the popup initially
				}
			}
			else {
				SearchResponse searchResponse = Db.getSearchResponse();
				if (searchResponse != null) {
					mHotelOverlay.setShowDistance(mShowDistances);
					mHotelOverlay.setProperties(searchResponse);
					mMapView.invalidate();
				}
			}
		}

		// Only show exact location overlay if we have a search lat/lng, and we're showing distance
		SearchParams params = Db.getSearchParams();
		if (mExactLocationOverlay != null) {
			if (params.hasSearchLatLon() && mShowDistances) {
				mExactLocationOverlay.setExactLocation(params.getSearchLatitude(), params.getSearchLongitude(),
						params.getSearchDisplayText(getActivity()));
			}
			else {
				mExactLocationOverlay.setExactLocation(0, 0, null);
			}
		}
	}

	private void animateToSearchLocation() {
		if (mMapView != null) {
			MapController mc = mMapView.getController();
			if (mShowSingleProperty) {
				mc.setZoom(16);
				mc.setCenter(mHotelOverlay.getCenter());
			}
			else {
				SearchParams params = Db.getSearchParams();
				GeoPoint searchPoint = MapUtils.convertToGeoPoint(params.getSearchLatitude(),
						params.getSearchLongitude());
				mc.animateTo(searchPoint);
				mc.setZoom(12);
			}
			updateView();
		}
	}

	/**
	 * Re-zooms/centers the map so that all results are visible
	 */
	private void showAllResults() {
		if (mMapView != null && mHotelOverlay != null) {
			MapController mc = mMapView.getController();
			mc.animateTo(mHotelOverlay.getCenter());
			mc.zoomToSpan(mHotelOverlay.getLatSpanE6(), mHotelOverlay.getLonSpanE6());
		}
	}

	private void selectBalloonForProperty() {
		// only select a balloon to be displayed over an overlay
		// item if there is a property whose overlay to display
		// and if there is view in which to display
		Property property = Db.getSelectedProperty();
		if (mHotelOverlay != null && property != null) {
			mHotelOverlay.showBalloon(property.getPropertyId(), BalloonItemizedOverlay.getDefaultFlags()
					+ BalloonItemizedOverlay.F_SILENCE_LISTENER);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface HotelMapFragmentListener {
		public void onBalloonShown(Property property);

		public void onBalloonClicked(Property property);
	}
}
