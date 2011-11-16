package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ConfirmationFragmentActivity;
import com.expedia.bookings.activity.ConfirmationFragmentActivity.InstanceFragment;
import com.expedia.bookings.activity.SearchFragmentActivity;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.widget.HotelItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.ImageCache;
import com.mobiata.android.MapUtils;

public class BookingConfirmationFragment extends Fragment {
	private MapView mMapView;

	public static BookingConfirmationFragment newInstance() {
		BookingConfirmationFragment fragment = new BookingConfirmationFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_confirmation, container, false);
		mMapView = MapUtils.createMapView(getActivity());
		mMapView.setClickable(true);
		ViewGroup mapViewLayout = (ViewGroup) view.findViewById(R.id.map_layout);
		mapViewLayout.addView(mMapView);
		mMapView.setEnabled(false);

		Property property = getInstance().mProperty;
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

		View shareBookingButton = view.findViewById(R.id.share_booking_info_button);
		shareBookingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String contactText = ConfirmationUtils.determineContactText(getActivity());
				InstanceFragment instance = getInstance();
				ConfirmationUtils.share(getActivity(), instance.mSearchParams, instance.mProperty,
						instance.mBookingResponse, instance.mBillingInfo, instance.mRate, contactText);
			}
		});

		View showOnMapButton = view.findViewById(R.id.show_on_map_button);
		showOnMapButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Property property = getInstance().mProperty;
				startActivity(ConfirmationUtils.generateIntentToShowPropertyOnMap(property));
			}
		});

		View nextSearchButton = view.findViewById(R.id.start_new_search_button);
		nextSearchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((ConfirmationFragmentActivity) getActivity()).newSearch();
			}
		});

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

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public ConfirmationFragmentActivity.InstanceFragment getInstance() {
		return ((ConfirmationFragmentActivity) getActivity()).mInstance;
	}
}
