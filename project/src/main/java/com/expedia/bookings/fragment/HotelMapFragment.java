package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelFilter.OnFilterChangedListener;
import com.expedia.bookings.data.HotelFilter.PriceRange;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.enums.ResultsHotelsState;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.GoogleMapsUtil;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
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
import com.squareup.picasso.Callback;

public class HotelMapFragment extends SupportMapFragment implements OnFilterChangedListener {
	public static final String INSTANCE_IS_HOTEL_RECEIPT = "INSTANCE_IS_HOTEL_RECEIPT";
	private static final String INSTANCE_INFO_WINDOW_SHOWING = "INSTANCE_INFO_WINDOW_SHOWING";
	private static final String EXACT_LOCATION_MARKER = "EXACT_LOCATION_MARKER";

	private static final float DEFAULT_ZOOM = 12.0f;

	private GoogleMap mMap;
	private LayoutInflater mInflater;

	private boolean mIsTablet = false;
	private boolean mShowDistances;
	private boolean mAddPropertiesWhenReady = false;
	private boolean mAddExactMarkerWhenReady = false;
	private boolean mShowAllWhenReady = false;

	private Marker mExactLocationMarker;

	private BitmapDescriptor mPin;
	private BitmapDescriptor mPinSale;
	private BitmapDescriptor mPinAirAttach;

	private Map<Property, Marker> mPropertiesToMarkers = new HashMap<>();
	private Map<Marker, Property> mMarkersToProperties = new HashMap<>();
	private Map<String, BitmapDescriptor> mPricePins = new HashMap<>();

	private HotelMapFragmentListener mListener;

	private String mInstanceInfoWindowShowing;

	// Data being displayed.  It is assumed that the overlay doesn't need
	// to keep track of state because the app will maintain this data
	private List<Property> mProperties;
	private int mResultsViewWidth;
	private boolean mShowInfoWindow = true;
	private int mFilterViewWidth;
	private int mCurrentLeftColWidth;

	private TextView mTextView;
	private int mPricePinSidePadding;
	private int mPricePinTopPadding;

	private boolean mIsFromHotelReceipt;

	private WeakHashMap<Marker, Boolean> markerMap = new WeakHashMap<>();

	public static HotelMapFragment newInstance() {
		return new HotelMapFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (mMap == null) {
			// To initialize CameraUpdateFactory and BitmapDescriptorFactory
			// since the GoogleMap is not ready
			MapsInitializer.initialize(activity);
		}

		mIsTablet = ExpediaBookingApp.useTabletInterface();
		if (mIsTablet) {
			mTextView = new TextView(getActivity());
			mTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mTextView.setGravity(Gravity.CENTER);
			mTextView.setTextColor(getResources().getColor(R.color.map_price_pin_text_color));
			mTextView.setTextSize(getResources().getDimension(R.dimen.hotel_map_price_pin_text_size));
			mTextView.setTypeface(null, Typeface.BOLD);
			float density = getResources().getDisplayMetrics().density;
			mPricePinSidePadding = (int) (7 * density);
			mPricePinTopPadding = (int) (5 * density);
		}

		mListener = Ui.findFragmentListener(this, HotelMapFragmentListener.class);
		mListener.onHotelMapFragmentAttached(this);
		Db.getFilter().addOnFilterChangedListener(this);
		runReadyActions();
		onRestoreSavedInstanceState(activity.getIntent().getExtras());
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Db.getFilter().removeOnFilterChangedListener(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mMap = getMap();
		if (ExpediaBookingApp.isAutomation()) {
			// We don't want to waste time loading tiles on an automation build
			mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
		}
		mInflater = LayoutInflater.from(getActivity());

		// Initial configuration
		GoogleMapsUtil.setMyLocationEnabled(getActivity(), mMap, true);
		mMap.getUiSettings().setZoomControlsEnabled(false);

		if (mIsTablet) {
			mMap.getUiSettings().setCompassEnabled(false);
		}

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
						if (mIsTablet && !marker.isInfoWindowShown()) {
							marker.showInfoWindow();
							focusProperty(property, true);
						}
						else {
							mListener.onPropertyClicked(property);
						}
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

		if (mIsTablet) {
			mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
				@Override
				public View getInfoContents(Marker marker) {
					return null;
				}

				@Override
				public View getInfoWindow(final Marker marker) {
					View v = mInflater.inflate(R.layout.snippet_map_hotel_info_window, null);
					Property property = mMarkersToProperties.get(marker);

					TextView name = Ui.findView(v, R.id.hotel_name);
					String hotelName = property.getName();
					if (hotelName.length() > 35) {
						hotelName = getString(R.string.ellipsize_text_template, hotelName.substring(0, 30));
					}
					name.setText(hotelName);

					final int totalReviews = property.getTotalReviews();
					if (totalReviews == 0) {
						Ui.findView(v, R.id.hotel_user_rating_container).setVisibility(View.GONE);
					}
					else {
						RatingBar ratingBar = Ui.findView(v, R.id.hotel_user_rating);
						ratingBar.setRating((float) property.getAverageExpediaRating());

						TextView numReviews = Ui.findView(v, R.id.hotel_number_reviews);
						numReviews.setText(getString(R.string.n_reviews_TEMPLATE, totalReviews));
					}

					TextView price = Ui.findView(v, R.id.hotel_price);
					Rate lowestRate = property.getLowestRate();
					if (lowestRate != null) {
						String formattedMoney = StrUtils.formatHotelPrice(lowestRate.getDisplayPrice());
						if (lowestRate.getUserPriceType() == Rate.UserPriceType.PER_NIGHT_RATE_NO_TAXES) {
							String formattedString = getString(R.string.From_x_per_night_template, formattedMoney);
							Spannable stringToSpan = new SpannableString(formattedString);
							int color = getResources().getColor(R.color.hotel_map_text_color);
							int startIndex = formattedString.indexOf(formattedMoney);
							Ui.setTextStyleBoldText(stringToSpan, color, startIndex,
								startIndex + formattedMoney.length());
							price.setText(stringToSpan);
						}
						else {
							price.setText(
								HtmlCompat.fromHtml(getString(R.string.map_snippet_price_template, formattedMoney)));
						}
					}
					else {
						price.setVisibility(View.GONE);
					}


					HotelMedia hotelMedia = property.getThumbnail();
					final ImageView imageView = Ui.findView(v, R.id.hotel_thumbnail);
					if (hotelMedia == null) {
						imageView.setVisibility(View.GONE);
					}
					else {
						List<String> urls = hotelMedia.getBestUrls(
							(int) (getResources().getDimension(R.dimen.hotel_map_popup_thumbnail_width)));
						Callback callback = new Callback() {
							@Override
							public void onSuccess() {
								if (!markerMap.containsKey(marker)) {
									markerMap.put(marker, true);
									marker.showInfoWindow();
								}
							}

							@Override
							public void onError() {

							}
						};
						new PicassoHelper.Builder(imageView).setCallback(callback).build().load(urls);
					}

					return v;
				}
			});
		}

		// Load graphics
		mPin = BitmapDescriptorFactory.fromResource(R.drawable.map_pin_normal);
		mPinSale = BitmapDescriptorFactory.fromResource(R.drawable.map_pin_sale);
		mPinAirAttach = BitmapDescriptorFactory.fromResource(R.drawable.map_pin_airattach);

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
		bundle.putBoolean(INSTANCE_IS_HOTEL_RECEIPT, mIsFromHotelReceipt);
	}

	public void onRestoreSavedInstanceState(Bundle bundle) {
		if (bundle == null) {
			initMapCameraToGoodSpot();
			return;
		}

		mInstanceInfoWindowShowing = bundle.getString(INSTANCE_INFO_WINDOW_SHOWING);
		mIsFromHotelReceipt = bundle.getBoolean(INSTANCE_IS_HOTEL_RECEIPT, false);
	}

	public boolean isReady() {
		return getActivity() != null && mMap != null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Control

	public void notifySearchStarted() {
		reset();
	}

	public void notifySearchComplete() {
		// #1303: Don't execute if not attached to Activity
		if (!isAdded()) {
			return;
		}

		showExactLocation();
		setSearchResponse(Db.getHotelSearch().getSearchResponse());
		if (isReady()) {
			showAll();
			checkIfSearchIsCurrentLocation();
		}
		else {
			mShowAllWhenReady = true;
		}
	}

	public void reset() {
		mAddPropertiesWhenReady = false;
		mAddExactMarkerWhenReady = false;
		mShowAllWhenReady = false;
		mProperties = null;
		if (mMap != null) {
			mMap.clear();
		}
		mPropertiesToMarkers.clear();
		mMarkersToProperties.clear();
		mPricePins.clear();
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
		if (Db.getHotelSearch().getSearchResponse() != null
			&& Db.getHotelSearch().getSearchParams().getSearchType() != null
			&& Db.getHotelSearch().getSearchParams().getSearchType().shouldShowExactLocation()) {
			HotelSearchParams params = Db.getHotelSearch().getSearchParams();
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

	public void setSearchResponse(HotelSearchResponse searchResponse) {
		if (searchResponse != null) {
			HotelSearchParams params = Db.getHotelSearch().getSearchParams();
			List<Property> properties = searchResponse.getFilteredAndSortedProperties(params);
			if (properties != null) {
				setProperties(properties);
			}
		}
	}

	public void setProperty(Property property) {
		List<Property> properties = new ArrayList<>();
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
		Rate lowestRate = property.getLowestRate();
		boolean isOnSale = lowestRate != null && lowestRate.isSaleTenPercentOrBetter();
		boolean isAirAttached = lowestRate != null && lowestRate.isAirAttached();
		// We don't care about this for tablet because we use an Info Window
		if (!mIsTablet) {
			String snippet = "";
			Distance distanceFromuser = property.getDistanceFromUser();

			if (lowestRate != null) {
				String formattedMoney = StrUtils.formatHotelPrice(lowestRate.getDisplayPrice());
				snippet = getString(R.string.map_snippet_price_template, formattedMoney);
			}
			String secondSnippet = null;
			if (mShowDistances && distanceFromuser != null) {
				secondSnippet = distanceFromuser.formatDistance(getActivity(), DistanceUnit.getDefaultDistanceUnit());
			}
			else if (isOnSale) {
				secondSnippet = getString(R.string.widget_savings_template, lowestRate.getDiscountPercent());
			}

			if (!TextUtils.isEmpty(secondSnippet)) {
				snippet = getString(R.string.map_snippet_template, snippet, secondSnippet);
			}

			if (!TextUtils.isEmpty(snippet) && !mIsFromHotelReceipt) {
				marker.snippet(snippet);
			}
			if (isOnSale) {
				if (isAirAttached) {
					marker.icon(mPinAirAttach);
				}
				else {
					marker.icon(mPinSale);
				}
			}
			else {
				marker.icon(mPin);
			}
		}
		else {
			marker.icon(mPin);
			if (lowestRate != null && mTextView != null) {
				final String label = lowestRate.getDisplayPrice().getFormattedMoney();
				BitmapDescriptor pin = mPricePins.get(label);
				if (pin == null) {
					mTextView.setText(label);
					int backgroundId;
					PriceRange range = Db.getHotelSearch().getSearchResponse().getPriceRange(property);
					switch (range) {
					case MODERATE:
						if (isOnSale && isAirAttached) {
							backgroundId = R.drawable.bg_tablet_hotel_price_pin_airattach_moderate;
						}
						else {
							backgroundId = R.drawable.bg_tablet_hotel_price_pin_moderate;
						}
						break;
					case EXPENSIVE:
						if (isOnSale && isAirAttached) {
							backgroundId = R.drawable.bg_tablet_hotel_price_pin_airattach_expensive;
						}
						else {
							backgroundId = R.drawable.bg_tablet_hotel_price_pin_expensive;
						}
						break;
					case CHEAP:
					case ALL:
					default:
						if (isOnSale && isAirAttached) {
							backgroundId = R.drawable.bg_tablet_hotel_price_pin_airattach_cheap;
						}
						else {
							backgroundId = R.drawable.bg_tablet_hotel_price_pin_cheap;
						}
						break;
					}
					mTextView.setBackgroundResource(backgroundId);
					mTextView.setPadding(mPricePinSidePadding, mPricePinTopPadding, mPricePinSidePadding,
						mPricePinTopPadding);
					mTextView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
					final int w = mTextView.getMeasuredWidth();
					final int h = mTextView.getMeasuredHeight();
					mTextView.layout(0, 0, w, h);
					Bitmap bitmap = Ui.createBitmapFromView(mTextView);

					pin = BitmapDescriptorFactory.fromBitmap(bitmap);
					mPricePins.put(label, pin);
				}
				marker.anchor(0.5f, 0.5f);
				marker.icon(pin);
			}
		}

		Marker actualMarker = mMap.addMarker(marker);

		mPropertiesToMarkers.put(property, actualMarker);
		mMarkersToProperties.put(actualMarker, property);

		if (mInstanceInfoWindowShowing != null && mInstanceInfoWindowShowing.equals(property.getPropertyId())) {
			actualMarker.showInfoWindow();
		}
	}

	public void notifyFilterChanged() {
		if (mProperties == null || Db.getHotelSearch().getSearchResponse() == null) {
			return;
		}

		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		List<Property> newSet = Db.getHotelSearch().getSearchResponse().getFilteredAndSortedProperties(params);

		// Add properties we have not seen.
		// This happens if we are already filtered,
		// map is created, then the filter constraints are relaxed.
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

	public void setShowDistances(boolean showDistances) {
		mShowDistances = showDistances;
	}

	public void focusProperty(Property property, boolean animate) {
		focusProperty(property, animate, -1.0f);
	}

	public void focusProperty(Property property, boolean animate, float zoom) {
		Marker marker = mPropertiesToMarkers.get(property);
		if (marker != null) {
			if (mShowInfoWindow) {
				marker.showInfoWindow();
			}
			CameraUpdate camUpdate;

			if (zoom != -1.0f) {
				LatLng position = offsetLatLng(marker.getPosition(), getCenterOffsetX(), getCenterOffsety(), zoom);
				camUpdate = CameraUpdateFactory.newLatLngZoom(position, zoom);
			}
			else {
				LatLng position = offsetLatLng(marker.getPosition());
				camUpdate = CameraUpdateFactory.newLatLng(position);
			}

			if (animate) {
				animateCamera(camUpdate);
			}
			else {
				moveCamera(camUpdate);
			}
		}
	}

	public void showBalloon(Property property) {
		Marker marker = mPropertiesToMarkers.get(property);
		marker.showInfoWindow();
	}

	public void hideBallon(Property property) {
		Marker marker = mPropertiesToMarkers.get(property);
		marker.hideInfoWindow();
	}

	public void setMapPaddingFromResultsHotelsState(ResultsHotelsState state) {
		int width;
		boolean filtersShowing = state == ResultsHotelsState.HOTEL_LIST_AND_FILTERS;
		if (filtersShowing) {
			width = mFilterViewWidth;
		}
		else if (state == ResultsHotelsState.MAP) {
			width = 0;
		}
		else {
			width = mResultsViewWidth;
		}
		setPadding(width, 0, 0, 0);
		mCurrentLeftColWidth = width;
		showAll();
	}

	/**
	 * Shows all properties visible on the map.
	 * <p>
	 * If there are properties but all are hidden (due to filtering),
	 * then it shows the area they would appear (if they weren't
	 * hidden).
	 */
	public void showAll() {
		Log.d("showAll Width: " + mCurrentLeftColWidth);
		setPadding(mCurrentLeftColWidth, 0, 0, 0);
		if (mProperties != null && mProperties.size() > 0) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			LatLngBounds.Builder allBuilder = new LatLngBounds.Builder();

			int numIncluded = 0;
			for (Property property : mProperties) {
				Marker marker = mPropertiesToMarkers.get(property);
				Location location = property.getLocation();
				LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
				if (marker != null && marker.isVisible()) {
					builder.include(latLng);
					numIncluded++;
				}

				if (numIncluded == 0) {
					allBuilder.include(latLng);
				}
			}

			if (numIncluded == 0) {
				builder = allBuilder;
			}

			animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),
				(int) getResources().getDisplayMetrics().density * 50));
		}
	}

	public LatLng getCameraCenter() {
		return getMap().getCameraPosition().target;
	}

	public void notifySearchLocationFound() {
		HotelSearchParams params = Db.getHotelSearch().getSearchParams();

		LatLng loc = new LatLng(params.getSearchLatitude(), params.getSearchLongitude());

		// For some Expedia-suggested locations they do not return a latitude/longitude; in those
		// cases, do not try to move the map around
		if (SupportMapFragment.isValidLatLng(loc)) {
			animateCamera(CameraUpdateFactory.newLatLngZoom(loc, DEFAULT_ZOOM));
		}
	}

	private void checkIfSearchIsCurrentLocation() {
		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		boolean showCurrentLocation = params.getSearchType() == HotelSearchParams.SearchType.MY_LOCATION;
		GoogleMapsUtil.setMyLocationEnabled(getActivity(), mMap, showCurrentLocation);
	}

	private void initMapCameraToGoodSpot() {
		setInitialCameraPosition(CameraUpdateFactory.newLatLngBounds(getAmericaBounds(), (int) getResources()
			.getDisplayMetrics().density * 50));
	}

	//////////////////////////////////////////////////////////////////////////
	// Listeners

	public void setResultsViewWidth(int resultsViewWidth) {
		mResultsViewWidth = resultsViewWidth;
	}

	public void setFilterViewWidth(int filterViewWidth) {
		mFilterViewWidth = filterViewWidth;
	}

	public void setShowInfoWindow(boolean showInfoWindow) {
		mShowInfoWindow = showInfoWindow;
	}

	@Override
	public void onFilterChanged() {
		notifyFilterChanged();
	}

	public void onHotelSelected(int tailOffset) {
		for (Marker marker : mMarkersToProperties.keySet()) {
			if (marker.isInfoWindowShown()) {
				marker.hideInfoWindow();
			}
		}
		setPadding(mCurrentLeftColWidth,
			getHeight() - getResources().getDimensionPixelSize(R.dimen.tablet_results_hotel_map_pin_padding)
				- getResources().getDimensionPixelOffset(R.dimen.tablet_hotel_details_top_padding) + tailOffset, 0,
			0
		);
		focusProperty(Db.getHotelSearch().getSelectedProperty(), true);
	}

	public interface HotelMapFragmentListener {
		void onMapClicked();

		void onPropertyClicked(Property property);

		void onExactLocationClicked();

		void onPropertyBubbleClicked(Property property);

		void onHotelMapFragmentAttached(HotelMapFragment fragment);
	}
}
