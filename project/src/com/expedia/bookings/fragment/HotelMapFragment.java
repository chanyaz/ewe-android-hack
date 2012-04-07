package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
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

	private MapView mMapView;
	private HotelItemizedOverlay mHotelOverlay;
	private DoubleTapToZoomListenerOverlay mDoubleTapToZoomOverlay;
	private ExactLocationItemizedOverlay mExactLocationOverlay;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Activity activity = getActivity();

		LinearLayout mapLayout = new LinearLayout(activity);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		mapLayout.setLayoutParams(params);

		mMapView = MapUtils.createMapView(activity);
		mMapView.setClickable(true);
		mapLayout.addView(mMapView);

		// Add the initial overlays
		List<Overlay> overlays = mMapView.getOverlays();

		mExactLocationOverlay = new ExactLocationItemizedOverlay(activity, mMapView);
		mExactLocationOverlay.setBalloonAdapter(new SimpleBalloonAdapter(getActivity()));
		overlays.add(mExactLocationOverlay);

		mHotelOverlay = new HotelItemizedOverlay(activity, null, mMapView);
		StandardBalloonAdapter adapter = new StandardBalloonAdapter(getActivity());
		adapter.setThumbnailPlaceholderResource(R.drawable.ic_image_placeholder);
		adapter.setShowChevron(false);
		mHotelOverlay.setBalloonListener(new BalloonListener() {
			@Override
			public void onBalloonShown(int index) {
				((SearchResultsFragmentActivity) getActivity()).propertySelected(mHotelOverlay.getProperty(index),
						SearchResultsFragmentActivity.SOURCE_MAP);
			}

			@Override
			public void onBalloonClicked(int index) {
				((SearchResultsFragmentActivity) getActivity())
						.moreDetailsForPropertySelected(SearchResultsFragmentActivity.SOURCE_MAP);
			}

			@Override
			public void onBalloonHidden() {
				// Do nothing
			}
		});

		mHotelOverlay.setCenterOffset(0, getResources().getDimensionPixelSize(R.dimen.mini_details_height));
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
	}

	@Override
	public void onDestroyView() {
		// remove the map view from the container so that its
		// view is not destroyed by the os to enable re-use
		((LinearLayout) getView()).removeAllViews();
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

	private void updateView() {
		// only update the view if the map view exists
		// and if there are overlay items to show on the map
		SearchResponse searchResponse = Db.getSearchResponse();
		if (mHotelOverlay != null && mMapView != null && searchResponse != null) {
			mHotelOverlay.setShowDistance(getInstance().mShowDistance);
			mHotelOverlay.setProperties(searchResponse);
			mMapView.invalidate();
		}

		// Only show exact location overlay if we have a search lat/lng, and we're showing distance
		SearchParams params = Db.getSearchParams();
		if (mExactLocationOverlay != null) {
			if (params.hasSearchLatLon() && getInstance().mShowDistance) {
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
			SearchParams params = Db.getSearchParams();
			GeoPoint searchPoint = MapUtils.convertToGeoPoint(params.getSearchLatitude(), params.getSearchLongitude());
			MapController mc = mMapView.getController();
			mc.animateTo(searchPoint);
			mc.setZoom(12);

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
	// Convenience method

	public SearchResultsFragmentActivity.InstanceFragment getInstance() {
		return ((SearchResultsFragmentActivity) getActivity()).mInstance;
	}
}
