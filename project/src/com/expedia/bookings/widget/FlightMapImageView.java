package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.TwoLevelImageCache.OnImageLoaded;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.GoogleServices.MapType;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;

public class FlightMapImageView extends ImageView {

	private static int DENSITY_SCALE_FACTOR = 1; // This has to be calculated at runtime

	private String mStaticMapUri;
	private List<Flight> mFlights;

	public FlightMapImageView(Context context) {
		super(context);
		initMapView();
	}

	public FlightMapImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FlightMapImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMapView();
	}

	private void initMapView() {
		Resources res = getResources();

		// High DPI screens should utilize scale=2 for this API
		// https://developers.google.com/maps/documentation/staticmaps/
		if (res.getDisplayMetrics().density > 1.5) {
			DENSITY_SCALE_FACTOR = 2;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w > 0 && h > 0) {
			regenerateUri();
		}
	}

	public void setFlights(List<Flight> flights) {
		mFlights = flights;
		regenerateUri();
	}

	private void regenerateUri() {
		if (mFlights == null || mFlights.size() == 0) {
			return;
		}

		int width = getWidth();
		int height = getResources().getDimensionPixelSize(R.dimen.hotel_details_map_visible_size) * 2;
		if (width == 0 || height == 0) {
			// It would be a useless image anyways
			return;
		}

		String oldUri = mStaticMapUri;

		mStaticMapUri = GoogleServices.getStaticPathMapUrl(width / DENSITY_SCALE_FACTOR, height
				/ DENSITY_SCALE_FACTOR, MapType.SATELLITE,
				buildStaticMapPathValueString(Color.WHITE, 4, true, mFlights),
				buildStaticMapMarkerStrings(mFlights));
		Log.d("ITIN: mapUrl:" + mStaticMapUri);

		if (!mStaticMapUri.equals(oldUri)) {
			UrlBitmapDrawable drawable = UrlBitmapDrawable.loadImageView(mStaticMapUri, this);
			drawable.setOnImageLoadedCallback(new OnImageLoaded() {
				@Override
				public void onImageLoaded(String url, Bitmap bitmap) {
					FlightMapImageView.this.setBackgroundDrawable(null);
				}

				@Override
				public void onImageLoadFailed(String url) {
					// nothing
				}
			});
		}
	}

	//path=color:0xFFFFFF%7Cweight:2%7Cgeodesic:true%7C40.644166,-73.782548%7C45.123456,-99.235632%7C37.6190,-122.3749
	public String buildStaticMapPathValueString(int color, int weight, boolean geodesic, List<Flight> flights) {
		StringBuilder builder = new StringBuilder();

		//Color
		builder.append("color:");
		builder.append(colorToHexString(color));
		builder.append("|");

		//Weight
		builder.append("weight:");
		builder.append(weight);
		builder.append("|");

		//Geodesic
		builder.append("geodesic:");
		builder.append(geodesic);
		builder.append("|");

		//Paths
		builder.append(flightListToCoordinateListString(flights));

		return builder.toString();
	}
	
	public List<String> buildStaticMapMarkerStrings(List<Flight> flights){
		ArrayList<String> markers = new ArrayList<String>();
		markers.add(flightListToCoordinateListString(flights));
		return markers;
	}

	private String colorToHexString(int color) {
		return String.format("0x%06X", (0xFFFFFF & color));
	}

	private String flightListToCoordinateListString(List<Flight> flights) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < flights.size(); i++) {
			Flight flight = flights.get(i);
			if (i == 0) {
				builder.append(waypointToCoords(flight.mOrigin));
			}
			builder.append("|");
			builder.append(waypointToCoords(flight.mDestination));

		}
		return builder.toString();
	}

	private String waypointToCoords(Waypoint wp) {
		return wp.getAirport().getLatitude() + "," + wp.getAirport().getLongitude();
	}

}
