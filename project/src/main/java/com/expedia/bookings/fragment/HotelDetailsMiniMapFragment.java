package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobiata.android.util.Ui;

public class HotelDetailsMiniMapFragment extends SupportMapFragment {

	private static final float ZOOM_LEVEL = 13;
	private GoogleMap mMap;

	public static HotelDetailsMiniMapFragment newInstance() {
		return new HotelDetailsMiniMapFragment();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mMap = getMap();

		// Initial configuration
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.getUiSettings().setZoomControlsEnabled(false);

		HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
		Property searchProperty = Db.getHotelSearch().getSelectedProperty();
		if (searchParams != null && searchProperty != null) {
			addMarker(searchProperty);
			checkIfSearchIsCurrentLocation(searchParams);
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(searchProperty.getLocation().getLatitude(), searchProperty.getLocation().getLongitude()), ZOOM_LEVEL));
		}
	}
	// Listener

	private void addMarker(Property property) {
		MarkerOptions marker = new MarkerOptions();
		Location location = property.getLocation();

		marker.position(new LatLng(location.getLatitude(), location.getLongitude()));

		Rate lowestRate = property.getLowestRate();
		boolean isOnSale = lowestRate != null && lowestRate.isSaleTenPercentOrBetter();
		marker.icon(isOnSale ? BitmapDescriptorFactory
			.fromResource(com.expedia.bookings.utils.Ui.obtainThemeResID(getActivity(), R.attr.skin_hotelListMapMarkerSaleDrawable)) : BitmapDescriptorFactory
			.fromResource(com.expedia.bookings.utils.Ui.obtainThemeResID(getActivity(), R.attr.skin_hotelListMapMarkerDrawable)));

		mMap.addMarker(marker);
	}

	private void checkIfSearchIsCurrentLocation(HotelSearchParams searchParams) {
		boolean showCurrentLocation = searchParams.getSearchType() == HotelSearchParams.SearchType.MY_LOCATION;
		mMap.setMyLocationEnabled(showCurrentLocation);
	}
}
