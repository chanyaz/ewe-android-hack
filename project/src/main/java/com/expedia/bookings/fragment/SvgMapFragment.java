package com.expedia.bookings.fragment;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.utils.Ui;
import com.jhlabs.map.Point2D;
import com.jhlabs.map.proj.MercatorProjection;
import com.jhlabs.map.proj.Projection;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

public class SvgMapFragment extends MeasurableFragment {

	private static final String ARG_MAP_RESOURCE = "ARG_MAP_RESOURCE";

	private static final float MAX_ZOOM = 4.0f;

	private View mMapView;

	private SVG mSvg;

	private Projection mProjection;
	private Matrix mViewportMatrix;
	private int mPaddingLeft = 0;
	private int mPaddingRight = 0;
	private int mPaddingTop = 0;
	private int mPaddingBottom = 0;
	private boolean mCrossesInternationalDateLine = false;

	private Point2D.Double mBottomRight;

	public static SvgMapFragment newInstance() {
		return new SvgMapFragment();
	}

	public void setMapResource(int resId) {
		Bundle args = getArguments();
		args.putInt(ARG_MAP_RESOURCE, resId);
		setArguments(args);
	}

	public SvgMapFragment() {
		Bundle args = new Bundle();
		setArguments(args);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		int mapResId = R.raw.map_tablet_launch;
		Bundle args = getArguments();
		if (args != null) {
			mapResId = args.getInt(ARG_MAP_RESOURCE, R.raw.map_tablet_launch);
		}

		mSvg = SVGParser.getSVGFromResource(context.getResources(), mapResId);

		mProjection = new MercatorProjection();
		double circumference = mProjection.getEllipsoid().getEquatorRadius() * 2 * Math.PI;
		mProjection.setFalseEasting(circumference / 2);
		mProjection.setFalseNorthing(circumference / 2);
		mProjection.setFromMetres((1 / circumference) * getSvgWidth());
		mProjection.initialize();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FrameLayout root = Ui.inflate(inflater, R.layout.fragment_svg_map, container, false);
		mMapView = Ui.findView(root, R.id.map_view);
		return root;
	}

	// WARNING!
	// This no longer works for n points
	public void setBounds(double... latlngs) {
		if (latlngs.length % 2 != 0) {
			throw new IllegalArgumentException("Must pass lat lng in pairs, found an odd number of arguments");
		}

		if (latlngs.length != 4) {
			throw new IllegalArgumentException("Must pass exactly 2 latlng pairs because of the shoddy international date line detection");
		}

		mCrossesInternationalDateLine = false;
		mBottomRight = null;

		double maxLat = -java.lang.Double.MAX_VALUE;
		double minLat = java.lang.Double.MAX_VALUE;

		double maxLng = -java.lang.Double.MAX_VALUE;
		double minLng = java.lang.Double.MAX_VALUE;

		for (int i = 0; i < latlngs.length; i += 2) {
			double lat = latlngs[i];
			double lng = latlngs[i + 1];

			maxLat = Math.max(maxLat, lat);
			minLat = Math.min(minLat, lat);
			maxLng = Math.max(maxLng, lng);
			minLng = Math.min(minLng, lng);
		}

		Point2D.Double tl = projectToSvg(maxLat, minLng);
		Point2D.Double br = projectToSvg(minLat, maxLng);

		if (Math.abs(br.x - tl.x) > Math.abs(tl.x - (br.x - getSvgWidth()))) {
			// It is shorter to go across the intl date line

			mCrossesInternationalDateLine = true;

			// We are going to shift br to the other side of the map
			double shifted = br.x - getSvgWidth();

			// br is no longer the right
			br.x = tl.x;
			tl.x = shifted;
		}

		mBottomRight = br;

		final float projectedWidth = (float) (br.x - tl.x);
		final float projectedHeight = (float) (br.y - tl.y);

		final int usableWidth = mMapView.getWidth() - mPaddingRight - mPaddingLeft;
		final float horizontalScale = usableWidth / projectedWidth;

		final int usableHeight = mMapView.getHeight() - mPaddingTop - mPaddingBottom;
		final float verticalScale = usableHeight / projectedHeight;

		float scale = Math.min(horizontalScale, verticalScale);
		scale = Math.min(MAX_ZOOM, scale); // Cap the zooming
		float yShift = 0.0f;
		float xShift = 0.0f;

		final float actualHeight = projectedHeight * scale;
		yShift = (usableHeight - actualHeight) / 2;
		final float actualWidth = projectedWidth * scale;
		xShift = (usableWidth - actualWidth) / 2;

		mViewportMatrix = new Matrix();
		mViewportMatrix.preTranslate((float) -(tl.x - mPaddingLeft / scale - xShift / scale), (float) -(tl.y - mPaddingTop / scale - yShift / scale));
		mViewportMatrix.postScale(scale, scale);
	}

	public Point2D.Double projectToSvg(double lat, double lon) {
		Point2D.Double p = new Point2D.Double();
		mProjection.transform(lon, lat, p);
		p.y = getSvgWidth() - p.y;

		if (mBottomRight != null && crossesInternationalDateLine()) {
			if (p.x > mBottomRight.x + 0.1d) {
				// We need to shift the point
				p.x -= getSvgWidth();
			}
		}
		return p;
	}

	public Point2D.Double projectToScreen(double lat, double lon) {
		// Project it into the svg coordinate system
		Point2D.Double t = projectToSvg(lat, lon);

		float[] pts = new float[2];
		pts[0] = (float) t.x;
		pts[1] = (float) t.y;

		// Project the point into our viewport
		mViewportMatrix.mapPoints(pts);

		t.x = pts[0];
		t.y = pts[1];

		return t;
	}

	public SVG getSvg() {
		return mSvg;
	}

	public void setMapView(View v) {
		mMapView = v;
	}

	public View getMapView() {
		return mMapView;
	}

	public Matrix getViewportMatrix() {
		return mViewportMatrix;
	}

	public boolean isMapGenerated() {
		return mViewportMatrix != null;
	}

	public boolean crossesInternationalDateLine() {
		return mCrossesInternationalDateLine;
	}

	public float getSvgWidth() {
		return 3000.0f;
	}

	public void setPadding(int left, int top, int right, int bottom) {
		mPaddingLeft = left;
		mPaddingTop = top;
		mPaddingRight = right;
		mPaddingBottom = bottom;
	}
}

