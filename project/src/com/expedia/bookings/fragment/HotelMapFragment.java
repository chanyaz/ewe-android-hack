package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.widget.HotelItemizedOverlay;
import com.expedia.bookings.widget.HotelItemizedOverlay.OnTapListener;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.MapUtils;
import com.mobiata.android.widget.DoubleTapToZoomListenerOverlay;

public class HotelMapFragment extends Fragment implements EventHandler {

	public static HotelMapFragment newInstance() {
		return new HotelMapFragment();
	}

	//////////////////////////////////////////////////////////////////////////
	// Member variables

	private MapView mMapView;
	private HotelItemizedOverlay mHotelOverlay;
	private DoubleTapToZoomListenerOverlay mDoubleTapToZoomOverlay;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		TabletActivity activity = (TabletActivity) getActivity();

		mMapView = activity.getMapView();

		// Add the initial overlays
		List<Overlay> overlays = mMapView.getOverlays();

		mHotelOverlay = new HotelItemizedOverlay(activity, null, false, mMapView, null);
		mHotelOverlay.setThumbnailPlaceholder(R.drawable.ic_image_placeholder);
		mHotelOverlay.setOnTapListener(new OnTapListener() {
			public boolean onTap(Property property) {
				((TabletActivity) getActivity()).propertySelected(property);
				return true;
			}
		});
		overlays.add(mHotelOverlay);

		mDoubleTapToZoomOverlay = new DoubleTapToZoomListenerOverlay(activity, mMapView);
		overlays.add(mDoubleTapToZoomOverlay);

		return mMapView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((TabletActivity) getActivity()).registerEventHandler(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		updateView();
		selectBalloonForProperty();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mMapView.getOverlays().clear();
		mHotelOverlay.destroyBalloon();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_SEARCH_STARTED:
			mHotelOverlay.setProperties(null);
			break;
		case TabletActivity.EVENT_SEARCH_LOCATION_FOUND:
			SearchParams params = ((TabletActivity) getActivity()).getSearchParams();
			GeoPoint searchPoint = MapUtils.convertToGeoPoint(params.getSearchLatitude(), params.getSearchLongitude());
			MapController mc = mMapView.getController();
			mc.animateTo(searchPoint);
			mc.setZoom(12);
			break;
		case TabletActivity.EVENT_SEARCH_COMPLETE:
			updateView();
			showAllResults();
			break;
		case TabletActivity.EVENT_PROPERTY_SELECTED:
			selectBalloonForProperty();
			break;
		case TabletActivity.EVENT_FILTER_CHANGED:
			mHotelOverlay.setProperties(((TabletActivity) getActivity()).getSearchResultsToDisplay());
			mMapView.invalidate();
			break;
		}
	}

	private void updateView() {
		// only update the view if the map view exists
		// and if there are overlay items to show on the map
		SearchResponse searchResponse = ((TabletActivity) getActivity()).getSearchResultsToDisplay();
		if (mHotelOverlay != null && mMapView != null && searchResponse != null) {
			mHotelOverlay.setProperties(searchResponse);
			mMapView.invalidate();
		}
	}

	/**
	 * Re-zooms/centers the map so that all results are visible
	 */
	private void showAllResults() {
		MapController mc = mMapView.getController();
		mc.animateTo(mHotelOverlay.getCenter());
		mc.zoomToSpan(mHotelOverlay.getLatSpanE6(), mHotelOverlay.getLonSpanE6());
	}

	private void selectBalloonForProperty() {
		// only select a balloon to be displayed over an overlay
		// item if there is a property whose overlay to display
		// and if there is view in which to display
		Property property = ((TabletActivity) getActivity()).getPropertyToDisplay();
		if (mHotelOverlay != null && property != null) {
			mHotelOverlay.showBalloon(property.getPropertyId(), true);
		}
	}
}
