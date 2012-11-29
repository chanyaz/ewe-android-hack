package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.GoogleServices.MapType;

public class MapImageView extends ImageView {

	private static int ZOOM = 13; // We want this to be different depending on dpi too
	private static int PIXEL_COEFFICIENT = 8192 * 256; // 2^ZOOM * map pixel width
	private static int DENSITY_SCALE_FACTOR = 1; // This has to be calculated at runtime

	private StaticMapPoint mCenterPoint;
	private StaticMapPoint mPoiPoint;

	private String mStaticMapUri;

	private int mCircleRadius;
	private Paint mPaint;
	private Bitmap mPoiBitmap;

	public MapImageView(Context context) {
		super(context);
		initMapView();
	}

	public MapImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MapImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMapView();
	}

	private void initMapView() {
		Resources res = getResources();

		// High DPI screens should utilize scale=2 for this API
		// https://developers.google.com/maps/documentation/staticmaps/
		if (res.getDisplayMetrics().density > 1.5) {
			DENSITY_SCALE_FACTOR = 2;
			ZOOM = 12;
			PIXEL_COEFFICIENT = 4096 * 256;
		}

		mPoiBitmap = BitmapFactory.decodeResource(res, R.drawable.search_center_purple);

		mCircleRadius = res.getDimensionPixelSize(R.dimen.mini_map_circle_radius);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w > 0 && h > 0) {
			regenerateUri();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw the hotel marker
		drawMarker(canvas, mCenterPoint);

		// Poi or my location
		if (mPoiPoint != null) {
			drawPoiMarker(canvas, mPoiPoint);
		}
	}

	public void setCenterPoint(Location location) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		mCenterPoint = new StaticMapPoint(latitude, longitude);
		regenerateUri();
	}

	private void regenerateUri() {
		if (mCenterPoint == null) {
			return;
		}

		double latitude = mCenterPoint.latitudeDeg;
		double longitude = mCenterPoint.longitudeDeg;

		int width = getWidth();
		int height = getResources().getDimensionPixelSize(R.dimen.hotel_details_map_visible_size) * 2;

		String oldUri = mStaticMapUri;

		mStaticMapUri = GoogleServices.getStaticMapUrl(width / DENSITY_SCALE_FACTOR, height / DENSITY_SCALE_FACTOR,
				ZOOM, MapType.ROADMAP, latitude, longitude) + "&scale=" + DENSITY_SCALE_FACTOR;

		if (!mStaticMapUri.equals(oldUri)) {
			UrlBitmapDrawable.loadImageView(mStaticMapUri, this);
		}
	}

	public void setPoiPoint(double latitude, double longitude) {
		mPoiPoint = new StaticMapPoint(latitude, longitude);
	}

	public String getUri() {
		return mStaticMapUri;
	}

	// Draws a marker on the appropriate place on the map, given its longitude and latitude
	private void drawPoiMarker(Canvas canvas, StaticMapPoint point) {
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

		canvas.drawBitmap(mPoiBitmap, x, y, null);
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

}
