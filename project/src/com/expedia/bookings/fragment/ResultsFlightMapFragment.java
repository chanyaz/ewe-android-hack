package com.expedia.bookings.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
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
import com.expedia.bookings.graphics.SvgDrawable;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightLineView;
import com.google.android.gms.maps.model.LatLng;
import com.jhlabs.map.Point2D;
import com.mobiata.android.maps.MapUtils;

/**
 * ResultsFlightMapFragment: The hotel map fragment designed for tablet results 2013
 */
public class ResultsFlightMapFragment extends SvgMapFragment {

	private FrameLayout mRoot;
	private View mMapView;
	private FlightLineView mFlightLine;
	private ImageView mDepartureImage;
	private ImageView mArrivalImage;

	private LayerDrawable mBgDrawable;

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
		mMapView = Ui.findView(mRoot, R.id.map_view);
		mFlightLine = Ui.findView(mRoot, R.id.flight_line_view);
		mDepartureImage = Ui.findView(mRoot, R.id.departure_image);
		mArrivalImage = Ui.findView(mRoot, R.id.arrival_image);

		setMapView(mMapView);

		mMapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			private Location mPrevDep = null;
			private Location mPrevArr = null;

			private int mPrevWidth = 0;
			private int mPrevHeight = 0;

			@Override
			public void onGlobalLayout() {
				if (!isAdded()) {
					return;
				}

				//Typically we dont need to do any work, but if our size changes, or our locations change, we really do need to do  work
				if (mPrevDep == null || mPrevArr == null
						|| mPrevDep != Db.getFlightSearch().getSearchParams().getDepartureLocation()
						|| mPrevArr != Db.getFlightSearch().getSearchParams().getArrivalLocation()
						|| mPrevWidth != mMapView.getWidth() || mPrevHeight != mMapView.getHeight()) {

					mPrevHeight = mMapView.getHeight();
					mPrevWidth = mMapView.getWidth();

					mPrevDep = Db.getFlightSearch().getSearchParams().getDepartureLocation();
					mPrevArr = Db.getFlightSearch().getSearchParams().getArrivalLocation();

					setDepartureLatLng(mPrevDep.getLatitude(), mPrevDep.getLongitude());
					setArrivalLatLng(mPrevArr.getLatitude(), mPrevArr.getLongitude());

					generateMap();
				}
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
		int w = getMapView().getWidth();
		int h = getMapView().getHeight();

		// TODO make work for pacific ocean
		setBounds(
				mDepartureLat, mDepartureLng,
				mArrivalLat, mArrivalLng);

		ColorDrawable bgColorDrawable = new ColorDrawable(Color.parseColor("#687887"));
		bgColorDrawable.setBounds(0, 0, w, h);

		SvgDrawable mapDrawable = new SvgDrawable(getSvg(), getViewportMatrix());
		mapDrawable.setBounds(0, 0, w, h);

		Drawable[] drawables = new Drawable[] {
			bgColorDrawable,
			mapDrawable,
		};
		mBgDrawable = new LayerDrawable(drawables);
		getMapView().setBackgroundDrawable(mBgDrawable);
		mRoot.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		positionFlightLine();
		positionDeparture();
		positionArrival();
	}

	private static final int NUM_SAMPLES = 31;

	private void positionFlightLine() {
		LatLng start = new LatLng(mDepartureLat, mDepartureLng);
		LatLng end = new LatLng(mArrivalLat, mArrivalLng);
		LatLng[] lineLatLngs = new LatLng[NUM_SAMPLES];
		Point2D.Double[] points = new Point2D.Double[NUM_SAMPLES];
		MapUtils.calculateGeodesicPolyline(start, end, lineLatLngs, null);

		for (int i = 0; i < lineLatLngs.length; i++) {
			LatLng firstLatLng = lineLatLngs[i];
			Point2D.Double point = projectToScreen(firstLatLng.latitude, firstLatLng.longitude);
			points[i] = point;
		}

		mFlightLine.setFlightLinePoints(points);
		mFlightLine.setupErasePaint(mBgDrawable);
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
