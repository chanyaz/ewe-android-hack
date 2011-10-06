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
import com.expedia.bookings.activity.TabletActivity.EventHandler;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.widget.HotelItemizedOverlay;
import com.expedia.bookings.widget.HotelItemizedOverlay.OnTapListener;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.mobiata.android.MapUtils;
import com.mobiata.android.widget.DoubleTapToZoomListenerOverlay;
import com.mobiata.android.widget.FixedMyLocationOverlay;

public class HotelMapFragment extends Fragment implements EventHandler {

	public static HotelMapFragment newInstance() {
		return new HotelMapFragment();
	}

	//////////////////////////////////////////////////////////////////////////
	// Member variables

	private MapView mMapView;
	private MyLocationOverlay mMyLocationOverlay;
	private HotelItemizedOverlay mHotelOverlay;
	private DoubleTapToZoomListenerOverlay mDoubleTapToZoomOverlay;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		((TabletActivity) activity).registerEventHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Context context = getActivity();

		mMapView = MapUtils.createMapView(context);

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

		mMyLocationOverlay = new FixedMyLocationOverlay(context, mMapView);
		overlays.add(mMyLocationOverlay);
		
		mDoubleTapToZoomOverlay = new DoubleTapToZoomListenerOverlay(context, mMapView);
		overlays.add(mDoubleTapToZoomOverlay);

		return mMapView;
	}

	@Override
	public void onStart() {
		super.onStart();

		mMyLocationOverlay.enableMyLocation();
	}

	@Override
	public void onStop() {
		super.onStop();

		mMyLocationOverlay.disableMyLocation();
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
			mHotelOverlay.setProperties((SearchResponse) data);
			break;
		case TabletActivity.EVENT_PROPERTY_SELECTED:
			mHotelOverlay.showBalloon(((Property) data).getPropertyId(), true);
			break;
		}
	}
}
