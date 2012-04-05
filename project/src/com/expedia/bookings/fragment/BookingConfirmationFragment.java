package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ConfirmationFragmentActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.tracking.Tracker;
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

		Property property = Db.getSelectedProperty();
		List<Property> properties = new ArrayList<Property>(1);
		properties.add(property);
		List<Overlay> overlays = mMapView.getOverlays();
		HotelItemizedOverlay overlay = new HotelItemizedOverlay(getActivity(), properties, mMapView);
		overlays.add(overlay);
		final MapController mc = mMapView.getController();
		mc.setZoom(15);
		mc.setCenter(overlay.getCenter());
		mMapView.post(new Runnable() {
			@Override
			public void run() {
				mc.setCenter(getAdjustedCenter(mMapView));
			}
		});

		// Thumbnail in the map
		ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail_image_view);
		if (property.getThumbnail() != null) {
			ImageCache.loadImage(property.getThumbnail().getUrl(), thumbnail);
		}
		else {
			thumbnail.setVisibility(View.GONE);
		}

		// anti-aliasing is not supported on the hardware
		// rendering pipline yet, so rendering the image 
		// on a software layer to prevent the jaggies.
		thumbnail.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		View shareBookingButton = view.findViewById(R.id.share_booking_info_button);
		shareBookingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String contactText = ConfirmationUtils.determineContactText(getActivity());
				ConfirmationUtils.share(getActivity(), Db.getSearchParams(), Db.getSelectedProperty(),
						Db.getBookingResponse(), Db.getBillingInfo(), Db.getSelectedRate(), contactText);
			}
		});

		View showOnMapButton = view.findViewById(R.id.show_on_map_button);
		showOnMapButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Tracker.trackViewOnMap(getActivity());

				startActivity(ConfirmationUtils.generateIntentToShowPropertyOnMap(Db.getSelectedProperty()));
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

	private GeoPoint getAdjustedCenter(MapView mapView) {
		GeoPoint center = mapView.getMapCenter();
		final int thirdDistance = (mapView.getLongitudeSpan() / 3);
		final int halfDistance = (mapView.getLongitudeSpan() / 2);
		final int moveDistance = halfDistance - thirdDistance;

		return new GeoPoint(center.getLatitudeE6(), center.getLongitudeE6() - moveDistance);
	}
}
