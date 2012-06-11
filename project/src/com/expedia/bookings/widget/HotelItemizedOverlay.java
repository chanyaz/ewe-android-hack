package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.utils.StrUtils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.mobiata.android.MapUtils;
import com.mobiata.android.widget.BalloonItemizedOverlay;
import com.mobiata.android.widget.StandardBalloonAdapter.StandardBalloonOverlayItem;

public class HotelItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {

	private static final int DEFAULT_PIN = R.drawable.map_pin_normal;

	private static final int DEFAULT_SPAN = 1000000;
	private Context mContext;
	private Drawable mMarkerSale;

	private List<Property> mProperties;
	private List<Property> mUnFilteredProperties;

	private boolean mShowDistance = true;

	private String mTappedPropertyId;

	private DistanceUnit mDistanceUnit;

	public HotelItemizedOverlay(Context context, List<Property> properties, MapView mapView) {
		super(boundCenterBottom(context.getResources().getDrawable(DEFAULT_PIN)), mapView);

		if (properties == null) {
			properties = new ArrayList<Property>();
		}

		mContext = context;
		mProperties = properties;
		mMarkerSale = context.getResources().getDrawable(R.drawable.map_pin_sale);
		mTappedPropertyId = null;
		mDistanceUnit = DistanceUnit.getDefaultDistanceUnit();

		populate();

		boundCenterBottom(mMarkerSale);

		setBalloonAdapter(new HotelBalloonAdapter());
	}

	public void setProperties(SearchResponse searchResponse) {
		hideBalloon();
		if (searchResponse != null) {
			Property[] propertyArray = searchResponse.getFilteredAndSortedProperties();
			mProperties = (propertyArray != null) ? Arrays.asList(propertyArray) : null;
			mUnFilteredProperties = searchResponse.getProperties();
		}
		else {
			mProperties = null;
			mUnFilteredProperties = null;
		}
		setLastFocusedIndex(-1);
		populate();
	}

	public void setShowDistance(boolean showDistance) {
		mShowDistance = showDistance;
	}

	/**
	 * This method returns true if hotels are visible
	 * in the current visible map area, false if not,
	 * and null if there are no hotels as overlay items
	 * to show on the map
	 * @return
	 */
	public Boolean areHotelsVisible() {
		MapView mapView = getMapView();
		int lonSpan = mapView.getLongitudeSpan();
		int latSpan = mapView.getLatitudeSpan();
		GeoPoint center = mapView.getMapCenter();

		if (mProperties == null || mProperties.isEmpty()) {
			return null;
		}

		for (Property property : mProperties) {
			Location location = property.getLocation();
			GeoPoint point = MapUtils.convertToGeoPoint(location.getLatitude(), location.getLongitude());
			if (MapUtils.isGeoPointWithinBound(point, center, latSpan, lonSpan)) {
				return Boolean.valueOf(true);
			}
		}

		return Boolean.valueOf(false);
	}

	public Property getProperty(int index) {
		return mProperties.get(index);
	}

	public String getTappedPropertyId() {
		return mTappedPropertyId;
	}

	@Override
	protected OverlayItem createItem(int i) {
		StandardBalloonOverlayItem overlayItem;
		Property property = mProperties.get(i);
		Location location = property.getLocation();
		GeoPoint point = MapUtils.convertToGeoPoint(location.getLatitude(), location.getLongitude());
		String snippet = "";

		Distance distanceFromuser = property.getDistanceFromUser();
		Rate lowestRate = property.getLowestRate();
		String formattedMoney = StrUtils.formatHotelPrice(lowestRate.getDisplayRate());
		snippet = mContext.getString(R.string.map_snippet_price_template, formattedMoney);

		if (mShowDistance && distanceFromuser != null) {
			snippet = mContext.getString(R.string.map_snippet_template, snippet,
					distanceFromuser.formatDistance(mContext, mDistanceUnit));
		}
		else if (lowestRate.isOnSale()) {
			snippet = mContext.getString(R.string.map_snippet_template, snippet,
					mContext.getString(R.string.widget_savings_template, lowestRate.getSavingsPercent() * 100));
		}

		overlayItem = new StandardBalloonOverlayItem(point, property.getName(), snippet);
		if (property.getThumbnail() != null) {
			overlayItem.setThumbnailUrl(property.getThumbnail().getUrl());
		}

		if (property.getLowestRate().isOnSale()) {
			overlayItem.setMarker(mMarkerSale);
		}

		return overlayItem;
	}

	@Override
	public int size() {
		if (mProperties != null) {
			return mProperties.size();
		}

		return 0;
	}

	@Override
	public GeoPoint getCenter() {
		double lat, lon;
		double minLat = 90;
		double maxLat = -90;
		double minLon = 180;
		double maxLon = -180;

		if (mProperties != null && mProperties.size() > 0) {
			for (Property property : mProperties) {
				Location location = property.getLocation();
				lat = location.getLatitude();
				lon = location.getLongitude();
				if (lat < minLat) {
					minLat = lat;
				}
				if (lat > maxLat) {
					maxLat = lat;
				}
				if (lon < minLon) {
					minLon = lon;
				}
				if (lon > maxLon) {
					maxLon = lon;
				}
			}

			return MapUtils.convertToGeoPoint(minLat + ((maxLat - minLat) / 2), minLon + ((maxLon - minLon) / 2));
		}

		else {
			// set the map to the center of north america if there are just no
			// properties (filtered or unfiltered) to display
			if (mUnFilteredProperties == null || mUnFilteredProperties.isEmpty()) {
				return MapUtils.convertToGeoPoint(44.674512553304, -103.85272979736);
			}

			// else, determine the center based on the unfiltered list of properties 
			List<GeoPoint> points = new ArrayList<GeoPoint>();
			for (Property property : mUnFilteredProperties) {
				points.add(MapUtils.convertToGeoPoint(property.getLocation().getLatitude(), property.getLocation()
						.getLongitude()));
			}

			return MapUtils.getCenter(points);
		}
	}

	@Override
	public int getLatSpanE6() {

		if (size() == 0) {
			return DEFAULT_SPAN;
		}
		return super.getLatSpanE6();
	}

	@Override
	public int getLonSpanE6() {
		// if there are no overlay items, 
		// zoom the map out to a default value
		if (size() == 0) {
			return DEFAULT_SPAN;
		}
		return super.getLonSpanE6();
	}

	@Override
	public void onBalloonShown(int index) {
		super.onBalloonShown(index);

		mTappedPropertyId = mProperties.get(index).getPropertyId();
	}

	public boolean showBalloon(String propertyId) {
		return showBalloon(propertyId, BalloonItemizedOverlay.F_FOCUS + BalloonItemizedOverlay.F_OFFSET_MARKER);
	}

	public boolean showBalloon(String propertyId, int flags) {

		for (int position = 0; mProperties != null && position < mProperties.size(); position++) {
			if (mProperties.get(position).getPropertyId().equals(propertyId)) {
				showBalloon(position, flags);
			}
		}

		return false;
	}

	@Override
	public void hideBalloon() {
		mTappedPropertyId = null;
		super.hideBalloon();
	}

	//////////////////////////////////////////////////////////////////////////
	// Custom BalloonAdapter

	private class HotelBalloonAdapter implements BalloonAdapter {

		private TextView mTitle;
		private TextView mSnippet;

		@Override
		public void bindView(View view, OverlayItem item, int index) {
			if (item.getTitle() != null) {
				mTitle.setVisibility(View.VISIBLE);
				mTitle.setText(item.getTitle());
			}
			else {
				mTitle.setVisibility(View.GONE);
			}

			if (item.getSnippet() != null) {
				mSnippet.setVisibility(View.VISIBLE);
				mSnippet.setText(item.getSnippet());
			}
			else {
				mSnippet.setVisibility(View.GONE);
			}
		}

		@Override
		public View newView(ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getMapView().getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);

			ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.balloon_map_hotel, null);

			mTitle = (TextView) layout.findViewById(R.id.balloon_item_title);
			mSnippet = (TextView) layout.findViewById(R.id.balloon_item_snippet);

			return layout;
		}
	}
}
