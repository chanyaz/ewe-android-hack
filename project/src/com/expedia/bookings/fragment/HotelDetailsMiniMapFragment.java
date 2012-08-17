package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.GoogleServices.MapType;

public class HotelDetailsMiniMapFragment extends Fragment {

	private static final int ZOOM = 12;
	private static final int PIXEL_COEFFICIENT = 4096 * 256; // 2^ZOOM * map pixel width
	private static int DENSITY_SCALE_FACTOR = 1; // This has to be calculated at runtime

	private ImageView mStaticMapImageView;
	private String mStaticMapUri;

	private StaticMapPoint mCenterPoint;
	private StaticMapPoint mPoiPoint;

	private HotelMiniMapFragmentListener mListener;

	public static HotelDetailsMiniMapFragment newInstance() {
		return new HotelDetailsMiniMapFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof HotelMiniMapFragmentListener)) {
			throw new RuntimeException(
					"HotelDetailsMiniMapFragment Activity must implement HotelMiniMapFragmentListener!");
		}

		mListener = (HotelMiniMapFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mStaticMapImageView = new MapImageView(getActivity());
		mStaticMapImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onMiniMapClicked();
			}
		});

		// High DPI screens should utilize scale=2 for this API
		// https://developers.google.com/maps/documentation/staticmaps/
		DENSITY_SCALE_FACTOR = getResources().getDisplayMetrics().density > 1.5 ? 2 : 1;

		return mStaticMapImageView;
	}

	public void populateViews() {
		SearchParams searchParams = Db.getSearchParams();
		Property searchProperty = Db.getSelectedProperty();
		double latitude = searchProperty.getLocation().getLatitude();
		double longitude = searchProperty.getLocation().getLongitude();
		mCenterPoint = new StaticMapPoint(latitude, longitude);

		// Fill in the POI / current location point appropriately
		switch (searchParams.getSearchType()) {
		case POI:
		case ADDRESS:
		case MY_LOCATION:
			//TODO: for EH 1.6
			//mPoiPoint = new StaticMapPoint(searchParams.getSearchLatitude(), searchParams.getSearchLongitude());
			break;
		}

		int width = mStaticMapImageView.getWidth();
		int height = mStaticMapImageView.getHeight();

		mStaticMapUri = GoogleServices.getStaticMapUrl(width / DENSITY_SCALE_FACTOR, height / DENSITY_SCALE_FACTOR,
				ZOOM, MapType.ROADMAP, latitude, longitude) + "&scale=" + DENSITY_SCALE_FACTOR;

		ImageCache.loadImage(mStaticMapUri, mStaticMapImageView);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (getActivity().isFinishing()) {
			Log.d("Clearing out map image.");

			ImageCache.removeImage(mStaticMapUri, true);
		}
	}

	private class StaticMapPoint {
		double latitudeDeg, longitudeDeg;
		double latitudeRad, longitudeRad;

		StaticMapPoint(double latitude, double longitude) {
			latitudeDeg = latitude;
			longitudeDeg = longitude;
			latitudeRad = latitude * Math.PI / 180;
			longitudeRad = longitude * Math.PI / 180;
		}
	}

	private class MapImageView extends ImageView {
		private int mCircleRadius;
		private Paint mPaint;

		public MapImageView(Context context) {
			super(context);

			Resources res = getResources();

			int width = ViewGroup.LayoutParams.MATCH_PARENT;
			int height = res.getDimensionPixelSize(R.dimen.hotel_details_map_visible_size) * 2;
			mCircleRadius = res.getDimensionPixelSize(R.dimen.mini_map_circle_radius);
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

			setScaleType(ScaleType.CENTER_CROP);
			setLayoutParams(new ViewGroup.LayoutParams(width, height));
			setImageResource(R.drawable.bg_gallery);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			super.onLayout(changed, left, top, right, bottom);

			if (changed && bottom - top > 0 && right - left > 0) {
				populateViews();
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			// Draw the hotel marker
			drawMarker(canvas, mCenterPoint);

			// Poi or my location
			if (mPoiPoint != null) {
				drawMarker(canvas, mPoiPoint);
			}
		}

		// Draws a marker on the appropriate place on the map, given its longitude and latitude
		private void drawMarker(Canvas canvas, StaticMapPoint point) {
			int x = getWidth() / 2;
			int y = getHeight() / 2;

			// The point of this math is to calculate the number of pixels between the center
			// of the map and the requested latitude/longitude. It's not totally straightforward,
			// due to the stretching of the map to fit it on a square (Mercator's projection).
			// http://blog.whatclinic.com/2008/10/how-to-make-google-static-maps-interactive.html
			if (point != mCenterPoint) {
				double deltaXdeg = point.longitudeDeg - mCenterPoint.longitudeDeg;
				double deltaXpx = (deltaXdeg / 360.0) * PIXEL_COEFFICIENT * DENSITY_SCALE_FACTOR;

				double centerYrad = mCenterPoint.latitudeRad;
				double centerYprojected = Math.log((1 + Math.sin(centerYrad)) / (1 - Math.sin(centerYrad))) / 2;
				double pointYrad = point.latitudeRad;
				double pointYprojected = Math.log((1 + Math.sin(pointYrad)) / (1 - Math.sin(pointYrad))) / 2;
				double deltaYprojected = centerYprojected - pointYprojected;
				double deltaYpx = (deltaYprojected / (2 * Math.PI)) * PIXEL_COEFFICIENT * DENSITY_SCALE_FACTOR;

				x += deltaXpx;
				y += deltaYpx;
			}

			mPaint.setStyle(Style.FILL);
			mPaint.setARGB(0xC0, 0x47, 0x71, 0x99);
			canvas.drawCircle(x, y, mCircleRadius, mPaint);

			mPaint.setStyle(Style.STROKE);
			mPaint.setStrokeWidth(2);
			mPaint.setColor(Color.BLACK);
			canvas.drawCircle(x, y, mCircleRadius, mPaint);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface HotelMiniMapFragmentListener {
		public void onMiniMapClicked();
	}
}
