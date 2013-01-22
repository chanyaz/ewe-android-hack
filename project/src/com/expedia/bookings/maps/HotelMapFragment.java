package com.expedia.bookings.maps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import android.view.ViewGroup;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.utils.StrUtils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.mobiata.android.Log;

public class HotelMapFragment extends SupportMapFragment {

	private static final String INSTANCE_INFO_WINDOW_SHOWING = "INSTANCE_INFO_WINDOW_SHOWING";
	private static final String EXACT_LOCATION_MARKER = "EXACT_LOCATION_MARKER";

	private GoogleMap mMap;

	private boolean mShowDistances;
	private boolean mAddPropertiesWhenReady = false;
	private boolean mAddExactMarkerWhenReady = false;

	private Marker mExactLocationMarker;

	private BitmapDescriptor mPin;
	private BitmapDescriptor mPinSale;

	private Map<Property, Marker> mPropertiesToMarkers = new HashMap<Property, Marker>();
	private Map<Marker, Property> mMarkersToProperties = new HashMap<Marker, Property>();

	private HotelMapFragmentListener mListener;

	private String mInstanceInfoWindowShowing;

	// Data being displayed.  It is assumed that the overlay doesn't need
	// to keep track of state because the app will maintain this data
	private List<Property> mProperties;
	private double mExactLocationLatitude;
	private double mExactLocationLongitude;
	private String mExactLocationTitle;

	private boolean mOffsetAnimation = false;
	private double mCenterOffsetY;

	public static HotelMapFragment newInstance() {
		HotelMapFragment frag = new HotelMapFragment();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof HotelMapFragmentListener)) {
			throw new RuntimeException("HotelMapFragment Activity must implement listener!");
		}

		if (mMap == null) {
			// To initialize CameraUpdateFactory and BitmapDescriptorFactory
			// since the GoogleMap is not ready
			try {
				MapsInitializer.initialize(activity);
			}
			catch (GooglePlayServicesNotAvailableException e) {
				Log.e("Google Play Services not availiable", e);
			}
		}

		mListener = (HotelMapFragmentListener) activity;
		mListener.onHotelMapFragmentAttached(this);

		runReadyActions();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mMap = getMap();

		// Initial configuration
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(false);

		mMap.setOnMapClickListener(new OnMapClickListener() {
			public void onMapClick(LatLng point) {
				if (mListener != null) {
					mListener.onMapClicked();
				}
			}
		});
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(Marker marker) {
				if (mListener != null) {
					Property property = mMarkersToProperties.get(marker);
					if (property != null) {
						mListener.onPropertyClicked(property);
					}
					else {
						mListener.onExactLocationClicked();
					}
				}
				return false;
			}
		});
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			public void onInfoWindowClick(Marker marker) {
				if (mListener != null) {
					mListener.onPropertyBubbleClicked(mMarkersToProperties.get(marker));
				}
			}
		});
		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			public void onCameraChange(CameraPosition position) {
				if (mOffsetAnimation && mCenterOffsetY != 0) {
					animateCamera(CameraUpdateFactory.scrollBy(0, (float) mCenterOffsetY / 2.0f));
					mOffsetAnimation = false;
				}
			}
		});

		// Load graphics
		mPin = BitmapDescriptorFactory.fromResource(R.drawable.map_pin_normal);
		mPinSale = BitmapDescriptorFactory.fromResource(R.drawable.map_pin_sale);

		runReadyActions();
	}

	// We have to save these since the GoogleMap was not ready
	// But we attached and received actions to perform
	private void runReadyActions() {
		if (isReady()) {
			if (mAddPropertiesWhenReady) {
				addProperties();
				mAddPropertiesWhenReady = false;
			}
			if (mAddExactMarkerWhenReady) {
				addExactLocation();
				mAddExactMarkerWhenReady = false;
			}
		}
	}

	public void onSaveInstanceState(Bundle bundle) {
		if (mExactLocationMarker != null && mExactLocationMarker.isInfoWindowShown()) {
			bundle.putString(INSTANCE_INFO_WINDOW_SHOWING, EXACT_LOCATION_MARKER);
		}
		else {
			for (Marker marker : mMarkersToProperties.keySet()) {
				if (marker.isInfoWindowShown()) {
					bundle.putString(INSTANCE_INFO_WINDOW_SHOWING, mMarkersToProperties.get(marker).getPropertyId());
				}
			}
		}
	}

	public void onRestoreSavedInstanceState(Bundle bundle) {
		mInstanceInfoWindowShowing = bundle.getString(INSTANCE_INFO_WINDOW_SHOWING);
	}

	private boolean isReady() {
		return getActivity() != null && mMap != null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Control

	public void notifySearchStarted() {
		reset();
	}

	public void notifySearchComplete() {
		showExactLocation(Db.getSearchParams());
		setSearchResponse(Db.getSearchResponse());
		showAll();
	}

	public void reset() {
		mMap.clear();
		mPropertiesToMarkers.clear();
		mMarkersToProperties.clear();
	}

	public void setCenterOffsetY(double offsetY) {
		mCenterOffsetY = offsetY;
	}

	public void showExactLocation(SearchParams params) {
		if (params.hasSearchLatLon() && params.getSearchType() != SearchParams.SearchType.MY_LOCATION) {
			showExactLocation(params.getSearchLatitude(), params.getSearchLongitude(),
					params.getSearchDisplayText(getActivity()));
			animateCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(params.getSearchLatitude(), params.getSearchLongitude()), 12.0f));
		}
	}

	public void showExactLocation(double lat, double lng, String title) {
		mExactLocationLatitude = lat;
		mExactLocationLongitude = lng;
		mExactLocationTitle = title;

		if (isReady()) {
			addExactLocation();
		}
		else {
			mAddExactMarkerWhenReady = true;
		}
	}

	// Only call this if isReady()
	private void addExactLocation() {
		if (mShowDistances) {
			LatLng point = new LatLng(mExactLocationLatitude, mExactLocationLongitude);

			if (mExactLocationMarker == null) {
				MarkerOptions marker = new MarkerOptions();
				marker.position(point);
				marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.search_center_purple));
				mExactLocationMarker = mMap.addMarker(marker);

				if (mInstanceInfoWindowShowing != null && mInstanceInfoWindowShowing.equals(EXACT_LOCATION_MARKER)) {
					mExactLocationMarker.showInfoWindow();
				}
			}

			mExactLocationMarker.setPosition(point);
			mExactLocationMarker.setTitle(mExactLocationTitle);
		}
	}

	public void setSearchResponse(SearchResponse searchResponse) {
		if (searchResponse != null && searchResponse.getFilteredAndSortedProperties() != null) {
			setProperties(Arrays.asList(searchResponse.getFilteredAndSortedProperties()));
		}
	}

	public void setProperty(Property property) {
		List<Property> properties = new ArrayList<Property>();
		properties.add(property);
		setProperties(properties);
	}

	public void setProperties(List<Property> properties) {
		mProperties = properties;

		if (isReady()) {
			addProperties();
		}
		else {
			mAddPropertiesWhenReady = true;
		}
	}

	// Only call this if isReady()
	private void addProperties() {
		// Add a marker for each property
		for (Property property : mProperties) {
			MarkerOptions marker = new MarkerOptions();

			Location location = property.getLocation();
			marker.position(new LatLng(location.getLatitude(), location.getLongitude()));

			marker.title(property.getName());

			String snippet = "";
			Distance distanceFromuser = property.getDistanceFromUser();
			Rate lowestRate = property.getLowestRate();
			String formattedMoney = StrUtils.formatHotelPrice(lowestRate.getDisplayRate());
			snippet = getString(R.string.map_snippet_price_template, formattedMoney);

			if (mShowDistances && distanceFromuser != null) {
				snippet = getString(R.string.map_snippet_template, snippet,
						distanceFromuser.formatDistance(getActivity(), DistanceUnit.getDefaultDistanceUnit()));
			}
			else if (lowestRate.isOnSale()) {
				snippet = getString(R.string.map_snippet_template, snippet,
						getString(R.string.widget_savings_template, lowestRate.getDiscountPercent() * 100));
			}

			marker.snippet(snippet);

			marker.icon((lowestRate.isOnSale()) ? mPinSale : mPin);

			Marker actualMarker = mMap.addMarker(marker);

			if (mInstanceInfoWindowShowing != null && mInstanceInfoWindowShowing.equals(property.getPropertyId())) {
				actualMarker.showInfoWindow();
			}

			mPropertiesToMarkers.put(property, actualMarker);
			mMarkersToProperties.put(actualMarker, property);
		}
	}

	public void notifyFilterChanged() {
		// Act as if an entire search has been done again.
		// TODO: This could be cleaned up a little in the future.
		if (mProperties != null) {
			reset();

			showExactLocation(mExactLocationLatitude, mExactLocationLongitude, mExactLocationTitle);
			setSearchResponse(Db.getSearchResponse());
			showAll();
		}
	}

	public void setShowDistances(boolean showDistances) {
		mShowDistances = showDistances;
	}

	public void focusProperty(Property property, boolean animate) {
		focusProperty(property, animate, -1.0f);
	}

	public void focusProperty(Property property, boolean animate, float zoom) {
		Marker marker = mPropertiesToMarkers.get(property);
		marker.showInfoWindow();
		CameraUpdate camUpdate;

		if (zoom != -1.0f) {
			camUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), zoom);
		}
		else {
			camUpdate = CameraUpdateFactory.newLatLng(marker.getPosition());
		}

		mOffsetAnimation = true;
		if (animate) {
			animateCamera(camUpdate);
		}
		else {
			moveCamera(camUpdate);
		}
	}

	public void showBalloon(Property property) {
		Marker marker = mPropertiesToMarkers.get(property);
		marker.showInfoWindow();
	}

	public void showAll() {
		if (mProperties != null) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();

			for (Property property : mProperties) {
				Location location = property.getLocation();
				builder.include(new LatLng(location.getLatitude(), location.getLongitude()));
			}

			animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),
						(int) getResources().getDisplayMetrics().density * 50));
		}
	}

	public void showExactLocationBalloon() {
		mExactLocationMarker.showInfoWindow();
	}

	public LatLng getCameraCenter() {
		return getMap().getCameraPosition().target;
	}

	//////////////////////////////////////////////////////////////////////////
	// Listeners

	public interface HotelMapFragmentListener {
		public void onMapClicked();

		public void onPropertyClicked(Property property);

		public void onExactLocationClicked();

		public void onPropertyBubbleClicked(Property property);

		public void onHotelMapFragmentAttached(HotelMapFragment fragment);
	}
}
