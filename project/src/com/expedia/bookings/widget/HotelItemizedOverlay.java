package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.utils.StrUtils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.mobiata.android.MapUtils;
import com.mobiata.android.widget.BalloonItemizedOverlay;
import com.mobiata.android.widget.BalloonOverlayItem;

public class HotelItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {

	private static final int DEFAULT_SPAN = 1000000;
	private Context mContext;
	private Drawable mMarkerUnavailable;
	private Drawable mMarkerHighlyRated;

	private List<Property> mProperties;
	private List<Property> mUnFilteredProperties;

	private boolean mOnClickEnabled;
	private boolean mShowDistance = true;

	private OnBalloonTap mOnBalloonTap;

	private String mTappedPropertyId;

	private DistanceUnit mDistanceUnit;

	private OnTapListener mOnTapListener;

	public HotelItemizedOverlay(Context context, List<Property> properties, boolean enableOnClick, MapView mapView,
			OnBalloonTap onTap) {
		super(boundCenterBottom(context.getResources().getDrawable(R.drawable.map_pin)), mapView);

		if (properties == null) {
			properties = new ArrayList<Property>();
		}

		mContext = context;
		mProperties = properties;
		mOnClickEnabled = enableOnClick;
		mOnBalloonTap = onTap;
		mMarkerUnavailable = context.getResources().getDrawable(R.drawable.map_pin_gray);
		mMarkerHighlyRated = context.getResources().getDrawable(R.drawable.map_pin_highly_rated);
		mTappedPropertyId = null;
		populate();

		boundCenterBottom(mMarkerUnavailable);
		boundCenterBottom(mMarkerHighlyRated);
		setBalloonBottomOffset(context.getResources().getDrawable(R.drawable.map_pin).getIntrinsicHeight());

		mDistanceUnit = DistanceUnit.getDefaultDistanceUnit();
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
				return new Boolean(true);
			}
		}

		return new Boolean(false);
	}

	public String getTappedPropertyId() {
		return mTappedPropertyId;
	}

	@Override
	protected OverlayItem createItem(int i) {
		BalloonOverlayItem overlayItem;
		Property property = mProperties.get(i);
		Location location = property.getLocation();
		GeoPoint point = MapUtils.convertToGeoPoint(location.getLatitude(), location.getLongitude());
		String snippet = "";

		Distance distanceFromuser = property.getDistanceFromUser();
		if (property.getLowestRate() != null) {
			String formattedMoney = StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate());
			String money = mContext.getString(R.string.map_snippet_price_template, formattedMoney);
			if (mShowDistance && distanceFromuser != null) {
				snippet = mContext.getString(R.string.map_snippet_template,
						distanceFromuser.formatDistance(mContext, mDistanceUnit), money);
			}
			else {
				snippet = money;
			}
		}
		else if (distanceFromuser != null) {
			snippet = distanceFromuser.formatDistance(mContext);
		}

		overlayItem = new BalloonOverlayItem(point, property.getName(), snippet);
		if (property.getThumbnail() != null) {
			overlayItem.setThumbnailUrl(property.getThumbnail().getUrl());
		}

		if (!property.isAvailable()) {
			overlayItem.setMarker(mMarkerUnavailable);
		}
		else if (property.isHighlyRated()) {
			overlayItem.setMarker(mMarkerHighlyRated);
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
		int numProps = mProperties.size();
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

		if (numProps > 0) {
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

	public interface OnTapListener {
		public boolean onTap(Property property);
	}

	@Override
	public boolean onTap(int index) {
		boolean resolved = false;
		if (mOnTapListener != null) {
			resolved = mOnTapListener.onTap(mProperties.get(index));
		}

		if (!resolved) {
			resolved = super.onTap(index);
		}

		return resolved;
	}

	public void setOnTapListener(OnTapListener onTapListener) {
		mOnTapListener = onTapListener;
	}

	@Override
	public boolean showBalloon(int index, boolean animateTo) {
		mTappedPropertyId = mProperties.get(index).getPropertyId();
		return super.showBalloon(index, animateTo);
	}

	public boolean showBalloon(String propertyId) {
		return showBalloon(propertyId, false);
	}

	public boolean showBalloon(String propertyId, boolean animateTo) {

		for (int position = 0; mProperties != null && position < mProperties.size(); position++) {
			if (mProperties.get(position).getPropertyId().equals(propertyId)) {
				return showBalloon(position, animateTo);
			}
		}

		return false;
	}

	@Override
	public void hideBalloon() {
		mTappedPropertyId = null;
		super.hideBalloon();
	}

	@Override
	protected boolean onBalloonTap(int index) {
		if (!mOnClickEnabled) {
			return false;
		}

		// Call the attached interface
		if (mOnBalloonTap != null) {
			mOnBalloonTap.onBalloonTap(mProperties.get(index));
			return true;
		}

		return false;
	}

	public interface OnBalloonTap {
		public void onBalloonTap(Property property);
	}
}
