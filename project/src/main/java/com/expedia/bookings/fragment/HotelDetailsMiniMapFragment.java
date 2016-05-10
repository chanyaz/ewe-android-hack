package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.utils.GoogleMapsUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class HotelDetailsMiniMapFragment extends SupportMapFragment {

	private static final float ZOOM_LEVEL = 13;
	private GoogleMap mMap;

	@Override
	public void onDestroy() {
		super.onDestroy();
		GoogleMapsUtil.setMyLocationEnabled(getActivity(), mMap, false);
	}

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
		boolean isAirAttach = lowestRate != null && lowestRate.isAirAttached();
		boolean isUserBucketedForSalePinGreenTest = Db.getAbacusResponse().isUserBucketedForTest(
			AbacusUtils.EBAndroidAppHotelHSRSalePinTest);
		int pinSaleAttrID;
		if (isUserBucketedForSalePinGreenTest) {
			pinSaleAttrID = R.drawable.map_pin_sale_green;
		}
		else {
			pinSaleAttrID = R.drawable.map_pin_sale;
		}

		if (isOnSale) {
			if (isAirAttach) {
				marker.icon(BitmapDescriptorFactory.fromResource(
					R.drawable.map_pin_airattach));
			}
			else {
				marker.icon(BitmapDescriptorFactory.fromResource(pinSaleAttrID));
			}
		}
		else {
			marker.icon(BitmapDescriptorFactory.fromResource(
				R.drawable.map_pin_normal));
		}
		mMap.addMarker(marker);
	}

	private void checkIfSearchIsCurrentLocation(HotelSearchParams searchParams) {
		boolean showCurrentLocation = searchParams.getSearchType() == HotelSearchParams.SearchType.MY_LOCATION;
		GoogleMapsUtil.setMyLocationEnabled(getActivity(), mMap, showCurrentLocation);
	}
}
