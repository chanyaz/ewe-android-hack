package com.expedia.bookings.fragment;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.utils.Ui;
import com.jhlabs.map.Point2D;
import com.jhlabs.map.proj.MercatorProjection;
import com.jhlabs.map.proj.Projection;
import com.larvalabs.svgandroid.SVGParser;

public class SvgMapFragment extends MeasurableFragment {

	private static final String ARG_MAP_RESOURCE = "ARG_MAP_RESOURCE";

	private FrameLayout mRoot;
	private ImageView mMapImageView;

	private Picture mPicture;

	private Projection mProjection;
	private Matrix mViewportMatrix;
	private int mPaddingLeft = 0;
	private int mPaddingRight = 0;
	private int mPaddingTop = 0;
	private int mPaddingBottom = 0;

	public static SvgMapFragment newInstance() {
		SvgMapFragment frag = new SvgMapFragment();
		return frag;
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
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		int mapResId = R.raw.map_tablet_launch;
		Bundle args = getArguments();
		if (args != null) {
			mapResId = args.getInt(ARG_MAP_RESOURCE, R.raw.map_tablet_launch);
		}

		mPicture = SVGParser.getSVGFromResource(activity.getResources(), mapResId).getPicture();

		mProjection = new MercatorProjection();
		double circumference = mProjection.getEllipsoid().getEquatorRadius() * 2 * Math.PI;
		mProjection.setFalseEasting(circumference / 2);
		mProjection.setFalseNorthing(circumference / 2);
		mProjection.setFromMetres((1 / circumference) * mPicture.getWidth());
		mProjection.initialize();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRoot = (FrameLayout) inflater.inflate(R.layout.fragment_svg_map, container, false);
		mMapImageView = Ui.findView(mRoot, R.id.map_image_view);
		return mRoot;
	}

	public void setHorizontalBounds(double lat0, double lng0, double lat1, double lng1) {
		Point2D.Double tl = projectToSvg(lat0, lng0);
		Point2D.Double tr = projectToSvg(lat1, lng1);

		float projectedWidth = (float) (tr.x - tl.x);
		float scale = mMapImageView.getWidth() / projectedWidth;

		mViewportMatrix = new Matrix();
		mViewportMatrix.preTranslate((float) -tl.x, (float) -tl.y);
		mViewportMatrix.postScale(scale, scale);
	}

	public void setBounds(double... latlngs) {
		if (latlngs.length % 2 != 0) {
			throw new IllegalArgumentException("Must pass lat lng in pairs, found an odd number of arguments");
		}

		double maxLat = - java.lang.Double.MAX_VALUE;
		double minLat = java.lang.Double.MAX_VALUE;

		double maxLng = - java.lang.Double.MAX_VALUE;
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

		float projectedWidth = (float) (br.x - tl.x);
		float projectedHeight = (float) (br.y - tl.y);

		int usableWidth = mMapImageView.getWidth() - mPaddingRight - mPaddingLeft;
		float horizontalScale =  usableWidth / projectedWidth;

		int usableHeight = mMapImageView.getHeight() - mPaddingTop - mPaddingBottom;
		float verticalScale = usableHeight / projectedHeight;

		float scale = Math.min(horizontalScale, verticalScale);
		float yShift = 0.0f;
		float xShift = 0.0f;

		float actualHeight = projectedHeight * scale;
		yShift = (usableHeight - actualHeight) / 2;
		float actualWidth = projectedWidth * scale;
		xShift = (usableWidth - actualWidth) / 2;

		mViewportMatrix = new Matrix();
		mViewportMatrix.preTranslate((float) -(tl.x - mPaddingLeft/scale), (float) -(tl.y - mPaddingTop/scale));
		mViewportMatrix.postScale(scale, scale);
		mViewportMatrix.postTranslate(xShift, yShift);
	}

	public Point2D.Double projectToSvg(double lat, double lon) {
		Point2D.Double p = new Point2D.Double();
		mProjection.transform(lon, lat, p);
		p.y = mPicture.getHeight() - p.y;
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

	public Picture getMapPicture() {
		return mPicture;
	}

	public ImageView getMapImageView() {
		return mMapImageView;
	}

	public Matrix getViewportMatrix() {
		return mViewportMatrix;
	}

	public Projection getSvgProjection() {
		return mProjection;
	}

	public void setPadding(int left, int top, int right, int bottom) {
		mPaddingLeft = left;
		mPaddingTop = top;
		mPaddingRight = right;
		mPaddingBottom = bottom;
	}
}

