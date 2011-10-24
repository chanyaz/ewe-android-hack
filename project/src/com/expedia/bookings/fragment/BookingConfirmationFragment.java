package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.widget.HotelItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.ImageCache;

public class BookingConfirmationFragment extends Fragment {
	private MapView mMapView;
	
	public static BookingConfirmationFragment newInstance() {
		BookingConfirmationFragment fragment = new BookingConfirmationFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_confirmation, container, false);
		mMapView = ((TabletActivity) getActivity()).getMapView();
		mMapView.setClickable(true);
		ViewGroup mapViewLayout = (ViewGroup) view.findViewById(R.id.map_layout);
		mapViewLayout.addView(mMapView);
		mMapView.setEnabled(false);
		
		Property property = ((TabletActivity) getActivity()).getPropertyToDisplay();
		List<Property> properties = new ArrayList<Property>(1);
		properties.add(property);
		List<Overlay> overlays = mMapView.getOverlays();
		HotelItemizedOverlay overlay = new HotelItemizedOverlay(getActivity(), properties, false, mMapView, null);
		overlays.add(overlay);
		MapController mc = mMapView.getController();
		GeoPoint center = overlay.getCenter();
		mc.setCenter(center);
		mc.setZoom(15);
		
		// Thumbnail in the map
		ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail_image_view);
		if (property.getThumbnail() != null) {
			ImageCache.loadImage(property.getThumbnail().getUrl(), thumbnail);
		}
		else {
			thumbnail.setVisibility(View.GONE);
		}

		return view;
	}

	@Override
	public void onDestroyView() {
		
		ViewGroup mapViewLayout = (ViewGroup) getView().findViewById(R.id.map_layout);
		mapViewLayout.removeView(mMapView);
		mMapView.setEnabled(true);
		mMapView.getOverlays().clear();
		super.onDestroyView();
	}
	
	

}
