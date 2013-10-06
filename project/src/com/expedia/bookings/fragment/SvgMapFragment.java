package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.os.Bundle;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.graphics.RoundBitmapDrawable;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCacheTypefaceSpan;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Ui;
import com.jhlabs.map.Point2D;
import com.jhlabs.map.proj.MercatorProjection;
import com.jhlabs.map.proj.Projection;
import com.larvalabs.svgandroid.SVGParser;
import com.mobiata.android.bitmaps.BitmapDrawable;

public class SvgMapFragment extends MeasurableFragment {

	private static final String ARG_MAP_RESOURCE = "ARG_MAP_RESOURCE";

	private FrameLayout mRoot;
	private ImageView mMapImageView;

	private LayoutInflater mInflater;
	private Picture mPicture;

	private Projection mProjection;
	private Matrix mMapMatrix;

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
		mInflater = LayoutInflater.from(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRoot = (FrameLayout) inflater.inflate(R.layout.fragment_tablet_launch_map, container, false);
		mMapImageView = Ui.findView(mRoot, R.id.map_image_view);

		mRoot.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				mRoot.getViewTreeObserver().removeOnPreDrawListener(this);
				generateMap();
				generatePins();
				return true;
			}
		});
		return mRoot;
	}

	public void generateMap() {
		mProjection = new MercatorProjection();
		double circumference = mProjection.getEllipsoid().getEquatorRadius() * 2 * Math.PI;
		mProjection.setFalseEasting(circumference / 2);
		mProjection.setFalseNorthing(circumference / 2);
		mProjection.setFromMetres((1 / circumference) * mPicture.getWidth());
		mProjection.initialize();

		int w = mMapImageView.getWidth();
		int h = mMapImageView.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		bitmap.eraseColor(Color.parseColor("#161f39"));

		Point2D.Double tl = transform(57, -140.828186);
		Point2D.Double tr = transform(57, 32.316284);

		float projectedWidth = (float) (tr.x - tl.x);
		float scale = mMapImageView.getWidth() / projectedWidth;

		mMapMatrix = new Matrix();
		mMapMatrix.preTranslate((float) -tl.x, (float) -tl.y);
		mMapMatrix.postScale(scale, scale);

		Canvas c = new Canvas(bitmap);
		c.setMatrix(mMapMatrix);
		mPicture.draw(c);

		mMapImageView.setImageDrawable(new BitmapDrawable(bitmap));
	}

	public void generatePins() {
		addPin(37.770715, -122.405033, "San Francisco", "From $320", R.drawable.mappin_sanfrancisco);
		addPin(41.893077, 12.481627, "Rome", "From $240", R.drawable.mappin_rome);
		addPin(40.425519, -3.709366, "Madrid", "From $450", R.drawable.mappin_madrid);
		addPin(25.797418, -80.226341, "Miami", "From $140", R.drawable.mappin_miami);
		addPin(45.525592, -73.553681, "Montreal", "From $100", R.drawable.mappin_montreal);
	}

	public void addPin(final double lat, final double lon, String name, String price, int drawableId) {
		final TextView pin = (TextView) mInflater.inflate(R.layout.snippet_tablet_launch_map_pin, mRoot, false);
		mRoot.addView(pin);
		setPinText(pin, name, price);
		setPinImage(pin, drawableId);

		pin.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				pin.getViewTreeObserver().removeOnPreDrawListener(this);

				// Position on screen now we know the pin dimensions
				Point2D.Double transformed = projectToScreen(lat, lon);
				int marginLeft = (int) (transformed.x - pin.getWidth() / 2);
				int marginTop = (int) (transformed.y - pin.getHeight() / 2);
				MarginLayoutParams lp = (MarginLayoutParams) pin.getLayoutParams();
				lp.setMargins(marginLeft, marginTop, 0, 0);
				pin.setLayoutParams(lp);

				// Popin animation
				pin.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				pin.setPivotX(pin.getWidth() / 2.0f);
				float mapPinImageSize = getResources().getDimension(R.dimen.tablet_launch_map_pin_image_size);
				pin.setPivotY(mapPinImageSize / 2.0f);

				pin.setScaleX(0.0f);
				pin.setScaleY(0.0f);
				ViewPropertyAnimator anim = pin.animate();
				anim.scaleX(1.0f);
				anim.scaleY(1.0f);
				anim.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator anim) {
						pin.setLayerType(View.LAYER_TYPE_NONE, null);
					}
				});
				anim.setInterpolator(new OvershootInterpolator(2.0f));
				anim.setDuration(500);
				anim.setStartDelay(400 + marginLeft / 5);
				anim.start();
				return true;
			}
		});
	}

	private void setPinText(TextView pin, String upper, String lower) {
		TextAppearanceSpan upperSpan = new TextAppearanceSpan(getActivity(), R.style.MapPinUpperTextAppearance);
		TextAppearanceSpan lowerSpan = new TextAppearanceSpan(getActivity(), R.style.MapPinLowerTextAppearance);
		FontCacheTypefaceSpan typefaceSpan = new FontCacheTypefaceSpan(FontCache.Font.ROBOTO_LIGHT);

		SpannableBuilder sb = new SpannableBuilder();
		sb.append(upper, upperSpan);
		sb.append("\n");
		sb.append(lower, lowerSpan, typefaceSpan);

		pin.setText(sb.build(), TextView.BufferType.SPANNABLE);
	}

	private void setPinImage(TextView pin, int drawableId) {
		RoundBitmapDrawable d = new RoundBitmapDrawable(getActivity(), drawableId);
		pin.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
	}

	private Point2D.Double transform(double lat, double lon) {
		Point2D.Double p = new Point2D.Double();
		mProjection.transform(lon, lat, p);
		p.y = mPicture.getHeight() - p.y;
		return p;
	}

	private Point2D.Double projectToScreen(double lat, double lon) {
		Point2D.Double t = transform(lat, lon);

		float[] pts = new float[2];
		pts[0] = (float) t.x;
		pts[1] = (float) t.y;

		// Transform the point into our viewport
		mMapMatrix.mapPoints(pts);

		t.x = pts[0];
		t.y = pts[1];

		return t;
	}
}

