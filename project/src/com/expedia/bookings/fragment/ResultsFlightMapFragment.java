package com.expedia.bookings.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.jhlabs.map.Point2D;
import com.mobiata.android.bitmaps.BitmapDrawable;

/**
 * ResultsFlightMapFragment: The hotel map fragment designed for tablet results 2013
 */
public class ResultsFlightMapFragment extends SvgMapFragment {

	private FrameLayout mRoot;

	private double mDepartureLat;
	private double mDepartureLng;
	private double mArrivalLat;
	private double mArrivalLng;

	public static ResultsFlightMapFragment newInstance() {
		ResultsFlightMapFragment frag = new ResultsFlightMapFragment();
		frag.setMapResource(R.raw.map_flight_details);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRoot = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);

		mRoot.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Location departure = Db.getFlightSearch().getSearchParams().getDepartureLocation();
				Location arrival = Db.getFlightSearch().getSearchParams().getArrivalLocation();
				setDepartureLatLng(departure.getLatitude(), departure.getLongitude());
				setArrivalLatLng(arrival.getLatitude(), arrival.getLongitude());

				generateMap();
			}
		});

		return mRoot;
	}

	public void setDepartureLatLng(double lat, double lng) {
		mDepartureLat = lat;
		mDepartureLng = lng;
	}

	public void setArrivalLatLng(double lat, double lng) {
		mArrivalLat = lat;
		mArrivalLng = lng;
	}

	private void generateMap() {
		int w = getMapImageView().getWidth();
		int h = getMapImageView().getHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		bitmap.eraseColor(Color.parseColor("#687887"));

		// TODO make work for pacific ocean
		setBounds(
			mDepartureLat, mDepartureLng,
			mArrivalLat, mArrivalLng
		);

		// Draw scaled and translated map
		Canvas c = new Canvas(bitmap);
		c.setMatrix(getViewportMatrix());
		getMapPicture().draw(c);
		c.setMatrix(new Matrix());

		float density = getResources().getDisplayMetrics().density;

		// TODO - all very temporary code to make the map slightly more functional
		Point2D.Double departureScreen = projectToScreen(mDepartureLat, mDepartureLng);
		Point2D.Double arrivalScreen = projectToScreen(mArrivalLat, mArrivalLng);

		Paint linePaint = new Paint();
		linePaint.setColor(0xFFFFFFFF);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(4 * density);
		linePaint.setAntiAlias(true);
		c.drawLine((float) departureScreen.x, (float) departureScreen.y, (float) arrivalScreen.x, (float) arrivalScreen.y, linePaint);

		Paint departurePaint = new Paint();
		departurePaint.setARGB(0xFF, 0x46, 0xC4, 0xFF);
		departurePaint.setAntiAlias(true);
		c.drawCircle((float) departureScreen.x, (float) departureScreen.y, 7 * density, departurePaint);

		Paint arrivalPaint = new Paint();
		arrivalPaint.setARGB(0xFF, 0xEC, 0xBD, 0x20);
		arrivalPaint.setAntiAlias(true);
		c.drawCircle((float) arrivalScreen.x, (float) arrivalScreen.y, 7 * density, arrivalPaint);

		getMapImageView().setImageDrawable(new BitmapDrawable(bitmap));
	}
}
