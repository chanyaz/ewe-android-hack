package com.expedia.bookings.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.utils.Ui;
import com.google.android.gms.maps.model.LatLng;
import com.jhlabs.map.Point2D;
import com.mobiata.android.bitmaps.BitmapDrawable;
import com.mobiata.android.maps.MapUtils;

/**
 * ResultsFlightMapFragment: The hotel map fragment designed for tablet results 2013
 */
public class ResultsFlightMapFragment extends SvgMapFragment {

	private FrameLayout mRoot;
	private ImageView mMapImageView;
	private ImageView mDepartureImage;
	private ImageView mArrivalImage;

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
		mRoot = (FrameLayout) inflater.inflate(R.layout.fragment_results_flight_map, container, false);
		mMapImageView = Ui.findView(mRoot, R.id.map_image_view);
		mDepartureImage = Ui.findView(mRoot, R.id.departure_image);
		mArrivalImage = Ui.findView(mRoot, R.id.arrival_image);

		setMapImageView(mMapImageView);
		mMapImageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (!isAdded()) {
					return;
				}

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

		final float density = getResources().getDisplayMetrics().density;

		// TODO - all very temporary code to make the map slightly more functional
		Point2D.Double departureScreen = projectToScreen(mDepartureLat, mDepartureLng);
		Point2D.Double arrivalScreen = projectToScreen(mArrivalLat, mArrivalLng);

		LatLng start = new LatLng(mDepartureLat, mDepartureLng);
		LatLng end = new LatLng(mArrivalLat, mArrivalLng);
		final int NUM_SAMPLES = 31;
		LatLng[] lineLatLngs = new LatLng[NUM_SAMPLES];
		MapUtils.calculateGeodesicPolyline(start, end, lineLatLngs, null);
		Path linePath = new Path();

		Paint linePaint = new Paint();
		linePaint.setColor(0xBBFFFFFF);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(4 * density);
		linePaint.setAntiAlias(true);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			LatLng firstLatLng = lineLatLngs[i];
			Point2D.Double point = projectToScreen(firstLatLng.latitude, firstLatLng.longitude);
			if (i == 0) {
				linePath.moveTo((float) point.x, (float) point.y);
			}
			else {
				linePath.lineTo((float) point.x, (float) point.y);
			}
		}
		c.drawPath(linePath, linePaint);

		getMapImageView().setImageDrawable(new BitmapDrawable(bitmap));
		positionDeparture();
		positionArrival();
	}

	private void positionDeparture() {
		Point2D.Double screen = projectToScreen(mDepartureLat, mDepartureLng);
		int left = (int) (screen.x - mDepartureImage.getWidth() / 2);
		int top = (int) (screen.y - mDepartureImage.getHeight() / 2);

		mDepartureImage.setTranslationX(left);
		mDepartureImage.setTranslationY(top);
	}

	private void positionArrival() {
		Point2D.Double screen = projectToScreen(mArrivalLat, mArrivalLng);
		int left = (int) (screen.x - mArrivalImage.getWidth() / 2);
		int top = (int) (screen.y - mArrivalImage.getHeight());

		mArrivalImage.setTranslationX(left);
		mArrivalImage.setTranslationY(top);
	}
}
