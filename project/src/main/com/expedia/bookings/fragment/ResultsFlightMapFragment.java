package com.expedia.bookings.fragment;

import android.graphics.Color;
import android.graphics.Matrix;
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

	private Drawable mBgDrawable;

	private double mDepartureLat;
	private double mDepartureLng;
	private double mArrivalLat;
	private double mArrivalLng;

	private final static String INSTANCE_IS_FORWARD = "INSTANCE_IS_FORWARD";
	private boolean mIsForward = true;
	private boolean mIsDepartureSet = false;
	private boolean mIsArrivalSet = false;

	public static ResultsFlightMapFragment newInstance() {
		ResultsFlightMapFragment frag = new ResultsFlightMapFragment();
		frag.setMapResource(R.raw.map_flight_details);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRoot = Ui.inflate(inflater, R.layout.fragment_results_flight_map, container, false);
		mMapView = Ui.findView(mRoot, R.id.map_view);
		mFlightLine = Ui.findView(mRoot, R.id.flight_line_view);
		mDepartureImage = Ui.findView(mRoot, R.id.departure_image);
		mArrivalImage = Ui.findView(mRoot, R.id.arrival_image);

		if (savedInstanceState != null) {
			savedInstanceState.getBoolean(INSTANCE_IS_FORWARD, true);
		}

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
						|| !mPrevDep.equals(Db.getFlightSearch().getSearchParams().getDepartureLocation())
						|| !mPrevArr.equals(Db.getFlightSearch().getSearchParams().getArrivalLocation())
						|| mPrevWidth != mMapView.getWidth() || mPrevHeight != mMapView.getHeight()) {

					mPrevHeight = mMapView.getHeight();
					mPrevWidth = mMapView.getWidth();

					mPrevDep = Db.getFlightSearch().getSearchParams().getDepartureLocation();
					mPrevArr = Db.getFlightSearch().getSearchParams().getArrivalLocation();

					if (mPrevDep != null && mPrevArr != null) {
						setDepartureLatLng(mPrevDep.getLatitude(), mPrevDep.getLongitude());
						setArrivalLatLng(mPrevArr.getLatitude(), mPrevArr.getLongitude());

						generateMap();
					}
				}
			}
		});

		return mRoot;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(INSTANCE_IS_FORWARD, mIsForward);
	}

	public void setDepartureLatLng(double lat, double lng) {
		mIsDepartureSet = true;
		mDepartureLat = lat;
		mDepartureLng = lng;
	}

	public void setArrivalLatLng(double lat, double lng) {
		mIsArrivalSet = true;
		mArrivalLat = lat;
		mArrivalLng = lng;
	}

	private void generateMap() {
		int w = getMapView().getWidth();
		int h = getMapView().getHeight();

		ColorDrawable bgColorDrawable = new ColorDrawable(Color.parseColor("#687887"));
		bgColorDrawable.setBounds(0, 0, w, h);

		SvgDrawable mapDrawable;

		// TODO make work for pacific ocean
		if (mIsDepartureSet && mIsArrivalSet) {
			setBounds(
				mDepartureLat, mDepartureLng,
				mArrivalLat, mArrivalLng
			);

			Drawable[] drawables;

			mapDrawable = new SvgDrawable(getSvg(), getViewportMatrix());
			mapDrawable.setBounds(0, 0, w, h);

			if (crossesInternationalDateLine()) {
				Matrix shifted = new Matrix(getViewportMatrix());
				shifted.preTranslate(-getSvgWidth(), 0);
				Drawable shiftedMapDrawable = new SvgDrawable(getSvg(), shifted);
				shiftedMapDrawable.setBounds(0, 0, w, h);

				drawables = new Drawable[] {
					bgColorDrawable,
					mapDrawable,
					shiftedMapDrawable,
				};
			}
			else {
				drawables = new Drawable[] {
					bgColorDrawable,
					mapDrawable,
				};
			}

			mBgDrawable = new LayerDrawable(drawables);
		}
		else {
			mBgDrawable = bgColorDrawable;
		}

		getMapView().setBackgroundDrawable(mBgDrawable);

		if (mIsDepartureSet && mIsArrivalSet) {
			positionFlightLine();
			if (mIsForward) {
				forward();
			}
			else {
				backward();
			}
		}
	}

	@Override
	public boolean isMapGenerated() {
		return super.isMapGenerated() && mIsDepartureSet && mIsArrivalSet;
	}

	public void forward() {
		mIsForward = true;
		mFlightLine.forward();
		positionPins(mDepartureLat, mDepartureLng, mArrivalLat, mArrivalLng);
	}

	// For reversing the flight line
	public void backward() {
		mIsForward = false;
		mFlightLine.backward();
		positionPins(mArrivalLat, mArrivalLng, mDepartureLat, mDepartureLng);
	}

	private void positionPins(double lat0, double lng0, double lat1, double lng1) {
		Point2D.Double screen;

		screen = projectToScreen(lat0, lng0);
		positionDeparturePin(screen);

		screen = projectToScreen(lat1, lng1);
		positionArrivalPin(screen);
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

	private void positionDeparturePin(Point2D.Double screen) {
		int left = (int) (screen.x - mDepartureImage.getWidth() / 2);
		int top = (int) (screen.y - mDepartureImage.getHeight() / 2);

		mDepartureImage.setTranslationX(left);
		mDepartureImage.setTranslationY(top);
	}

	private void positionArrivalPin(Point2D.Double screen) {
		int left = (int) (screen.x - mArrivalImage.getWidth() / 2);
		int top = (int) (screen.y - mArrivalImage.getHeight());

		mArrivalImage.setTranslationX(left);
		mArrivalImage.setTranslationY(top);
	}

	@Override
	public void setPadding(int l, int t, int r, int b) {
		super.setPadding(l, t, r, b);
		if (isMapGenerated()) {
			generateMap();
		}
	}
}
