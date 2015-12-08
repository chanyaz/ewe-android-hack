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
import com.expedia.bookings.utils.Ui;
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
			pinSaleAttrID = R.attr.skin_hotelListMapMarkerSaleGreenABTestDrawable;
		}
		else {
			pinSaleAttrID = R.attr.skin_hotelListMapMarkerSaleDrawable;
		}

		if (isOnSale) {
			if (isAirAttach) {
				marker.icon(BitmapDescriptorFactory.fromResource(
					Ui.obtainThemeResID(getActivity(), R.attr.skin_hotelListMapMarkerAirAttachDrawable)));
			}
			else {
				marker.icon(BitmapDescriptorFactory.fromResource(Ui.obtainThemeResID(getActivity(), pinSaleAttrID)));
			}
		}
		else {
			marker.icon(BitmapDescriptorFactory.fromResource(
				Ui.obtainThemeResID(getActivity(), R.attr.skin_hotelListMapMarkerDrawable)));
		}
		mMap.addMarker(marker);
	}

	private void checkIfSearchIsCurrentLocation(HotelSearchParams searchParams) {
		boolean showCurrentLocation = searchParams.getSearchType() == HotelSearchParams.SearchType.MY_LOCATION;
		GoogleMapsUtil.setMyLocationEnabled(getActivity(), mMap, showCurrentLocation);
	}
}
