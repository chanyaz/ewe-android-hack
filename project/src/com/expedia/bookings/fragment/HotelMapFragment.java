package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.widget.HotelItemizedOverlay;
import com.expedia.bookings.widget.HotelItemizedOverlay.OnTapListener;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.MapUtils;
import com.mobiata.android.widget.DoubleTapToZoomListenerOverlay;

public class HotelMapFragment extends Fragment implements EventHandler, OnFilterChangedListener {

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
		Context context = getActivity();
		// create a new map view belonging to the map activity
		// if there is no mapView that already exists or if the current hosting
		// activity of this fragment is different than the activity to which the map
		// view belongs (in all probability, a pointer to the activity prior to device rotation)
		if (mMapView == null || mMapView.getContext() != getActivity()) {
			mMapView = MapUtils.createMapView(context);
		}

		// Add the initial overlays
		List<Overlay> overlays = mMapView.getOverlays();

		mHotelOverlay = new HotelItemizedOverlay(context, null, false, mMapView, null);
		mHotelOverlay.setThumbnailPlaceholder(R.drawable.ic_image_placeholder);
		mHotelOverlay.setOnTapListener(new OnTapListener() {
			public boolean onTap(Property property) {
				((TabletActivity) getActivity()).propertySelected(property);
				return true;
			}
		});
		overlays.add(mHotelOverlay);

		mDoubleTapToZoomOverlay = new DoubleTapToZoomListenerOverlay(context, mMapView);
		overlays.add(mDoubleTapToZoomOverlay);

		return mMapView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((TabletActivity) getActivity()).registerEventHandler(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		SearchResponse searchResponse = ((TabletActivity) getActivity()).getSearchResultsToDisplay();
		if (searchResponse != null) {
			searchResponse.getFilter().addOnFilterChangedListener(this);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		updateView();
		selectBalloonForProperty();
	}

	@Override
	public void onStop() {
		super.onStop();

		SearchResponse searchResponse = ((TabletActivity) getActivity()).getSearchResultsToDisplay();
		if (searchResponse != null) {
			searchResponse.getFilter().removeOnFilterChangedListener(this);
		}
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
		case TabletActivity.EVENT_SEARCH_COMPLETE:
			updateView();
			((TabletActivity) getActivity()).getSearchResultsToDisplay().getFilter().addOnFilterChangedListener(this);
			break;
		case TabletActivity.EVENT_PROPERTY_SELECTED:
			selectBalloonForProperty();
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

	private void selectBalloonForProperty() {
		// only select a balloon to be displayed over an overlay
		// item if there is a property whose overlay to display
		// and if there is view in which to display
		Property property = ((TabletActivity) getActivity()).getPropertyToDisplay();
		if (mHotelOverlay != null && property != null) {
			mHotelOverlay.showBalloon(property.getPropertyId(), true);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// OnFilterChangedListener implementation

	@Override
	public void onFilterChanged() {
		mHotelOverlay.setProperties(((TabletActivity) getActivity()).getSearchResultsToDisplay());
		mMapView.invalidate();
	}
}
