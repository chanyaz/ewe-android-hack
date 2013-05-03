package com.expedia.bookings.maps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Distance.DistanceUnit;
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
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobiata.android.Log;

public class HotelMapFragment extends SupportMapFragment {

	private static final String INSTANCE_INFO_WINDOW_SHOWING = "INSTANCE_INFO_WINDOW_SHOWING";
	private static final String EXACT_LOCATION_MARKER = "EXACT_LOCATION_MARKER";

	private static float DEFAULT_ZOOM = 12.0f;

	private GoogleMap mMap;

	private boolean mShowDistances;
	private boolean mAddPropertiesWhenReady = false;
	private boolean mAddExactMarkerWhenReady = false;
	private boolean mShowAllWhenReady = false;

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

				// We will focusProperty on our own, so we want to handle the event
				return true;
			}
		});
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			public void onInfoWindowClick(Marker marker) {
				if (mListener != null) {
					mListener.onPropertyBubbleClicked(mMarkersToProperties.get(marker));
				}
			}
		});

		// Load graphics
		mPin = BitmapDescriptorFactory.fromResource(R.drawable.map_pin_normal);
		mPinSale = BitmapDescriptorFactory.fromResource(R.drawable.map_pin_sale);

		onRestoreSavedInstanceState(savedInstanceState);
		runReadyActions();
	}

	// We have to save these since the GoogleMap was not ready
	// But we attached and received actions to perform
	private void runReadyActions() {
		if (isReady()) {
			checkIfSearchIsCurrentLocation();

			if (mAddPropertiesWhenReady) {
				addProperties();
				mAddPropertiesWhenReady = false;
			}
			if (mAddExactMarkerWhenReady) {
				addExactLocation();
				mAddExactMarkerWhenReady = false;
			}
			if (mShowAllWhenReady) {
				showAll();
				mShowAllWhenReady = false;
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);

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
		if (bundle == null) {
			initMapCameraToGoodSpot();
			return;
		}

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
		showExactLocation();
		setSearchResponse(Db.getSearchResponse());
		if (isReady()) {
			showAll();
			checkIfSearchIsCurrentLocation();
		}
		else {
			mShowAllWhenReady = true;
		}
	}

	public void reset() {
		mProperties = null;
		if (mMap != null) {
			mMap.clear();
		}
		mPropertiesToMarkers.clear();
		mMarkersToProperties.clear();
		mExactLocationMarker = null;
	}

	private void showExactLocation() {
		if (isReady()) {
			addExactLocation();
		}
		else {
			mAddExactMarkerWhenReady = true;
		}
	}

	// Only call this if isReady()
	private void addExactLocation() {
		if (Db.getSearchResponse() != null
				&& Db.getSearchResponse().getSearchType() != null
				&& Db.getSearchResponse().getSearchType().shouldShowExactLocation()) {
			SearchParams params = Db.getSearchParams();
			LatLng point = new LatLng(params.getSearchLatitude(), params.getSearchLongitude());

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
			mExactLocationMarker.setTitle(params.getSearchDisplayText(getActivity()));
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
			addMarker(property);
		}
	}

	private void addMarker(Property property) {
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
					getString(R.string.widget_savings_template, lowestRate.getDiscountPercent()));
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

	public void notifyFilterChanged() {
		if (mProperties != null && Db.getSearchResponse() != null) {
			List<Property> newSet = Arrays.asList(Db.getSearchResponse().getFilteredAndSortedProperties());

			// Add properties we have not seen.
			// This happens if we are already filtered,
			// map is created, then the filter contraints are relaxed.
			if (newSet.size() > mProperties.size()) {
				for (Property property : newSet) {
					if (!mProperties.contains(property)) {
						addMarker(property);
					}
				}
			}

			// Toggle visibility
			for (Property property : mProperties) {
				boolean visibility = newSet.contains(property);
				Marker marker = mPropertiesToMarkers.get(property);
				marker.setVisible(visibility);
			}
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

		LatLng position = offsetLatLng(marker.getPosition());
		if (zoom != -1.0f) {
			camUpdate = CameraUpdateFactory.newLatLngZoom(position, zoom);
		}
		else {
			camUpdate = CameraUpdateFactory.newLatLng(position);
		}

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

	/**
	 * Shows all properties visible on the map.
	 *
	 * If there are properties but all are hidden (due to filtering),
	 * then it shows the area they would appear (if they weren't
	 * hidden).
	 */
	public void showAll() {
		if (mProperties != null && mProperties.size() > 0) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			LatLngBounds.Builder allBuilder = new LatLngBounds.Builder();

			int numIncluded = 0;
			for (Property property : mProperties) {
				Marker marker = mPropertiesToMarkers.get(property);
				Location location = property.getLocation();
				LatLng latLng = offsetLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
				if (marker != null && marker.isVisible()) {
					builder.include(latLng);
					numIncluded++;
				}

				allBuilder.include(latLng);
			}

			if (numIncluded == 0) {
				builder = allBuilder;
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

	public void notifySearchLocationFound() {
		SearchParams params = Db.getSearchParams();


		LatLng loc = new LatLng(params.getSearchLatitude(), params.getSearchLongitude());

		// For some Expedia-suggested locations they do not return a latitude/longitude; in those
		// cases, do not try to move the map around
		if (SupportMapFragment.isValidLatLng(loc)) {
			animateCamera(CameraUpdateFactory.newLatLngZoom(loc , DEFAULT_ZOOM));
		}
	}

	public void notifyPropertySelected() {
		showBalloon(Db.getSelectedProperty());
		focusProperty(Db.getSelectedProperty(), true);
	}

	private void checkIfSearchIsCurrentLocation() {
		SearchParams params = Db.getSearchParams();
		boolean showCurrentLocation = params.getSearchType() == SearchParams.SearchType.MY_LOCATION;
		if (mMap != null) {
			mMap.setMyLocationEnabled(showCurrentLocation);
		}
	}

	private void initMapCameraToGoodSpot() {
		setInitialCameraPosition(CameraUpdateFactory.newLatLngBounds(getAmericaBounds(),
				(int) getResources().getDisplayMetrics().density * 50));
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
