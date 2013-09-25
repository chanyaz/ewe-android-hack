package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
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
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCacheTypefaceSpan;
import com.expedia.bookings.utils.Ui;
import com.jhlabs.map.Point2D;
import com.jhlabs.map.proj.MercatorProjection;
import com.jhlabs.map.proj.Projection;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.BitmapDrawable;

public class SvgMapFragment extends MeasurableFragment {
	private FrameLayout mRoot;
	private Picture mPicture;
	private Projection mProjection;
	private ImageView mMapImageView;
	private LayoutInflater mInflater;
	private float mPinDimension;

	private Matrix mMapMatrix;

	public static SvgMapFragment newInstance() {
		SvgMapFragment frag = new SvgMapFragment();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mPicture = SVGParser.getSVGFromResource(activity.getResources(), R.raw.wallpaper_bg_night).getPicture();
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
		mPinDimension = getResources().getDimension(R.dimen.tablet_launch_map_pin_size);

		mProjection = new MercatorProjection();
		double circumference = mProjection.getEllipsoid().getEquatorRadius() * 2 * Math.PI;
		mProjection.setFalseEasting(circumference / 2);
		mProjection.setFalseNorthing(circumference / 2);
		mProjection.setFromMetres((1 / circumference) * mPicture.getWidth());
		mProjection.initialize();

		int w = mMapImageView.getWidth();
		int h = mMapImageView.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		bitmap.eraseColor(Color.TRANSPARENT);

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

	public void addPin(double lat, double lon, String name, String price, int drawableId) {
		final TextView pin = (TextView) mInflater.inflate(R.layout.snippet_tablet_launch_map_pin, mRoot, false);
		mRoot.addView(pin);
		setPinText(pin, name, price);
		setPinImage(pin, drawableId);

		Point2D.Double transformedPoint = transform(lat, lon);

		float[] pts = new float[2];
		pts[0] = (float) transformedPoint.x;
		pts[1] = (float) transformedPoint.y;

		// Transform the point into our viewport
		mMapMatrix.mapPoints(pts);

		final int marginLeft = (int) (pts[0] - mPinDimension / 2);
		int marginTop = (int) (pts[1] - mPinDimension / 2);

		MarginLayoutParams lp = (MarginLayoutParams) pin.getLayoutParams();
		lp.setMargins(marginLeft, marginTop, 0, 0);
		pin.setLayoutParams(lp);
		pin.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				pin.getViewTreeObserver().removeOnPreDrawListener(this);
				pin.setLayerType(View.LAYER_TYPE_HARDWARE, null);
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

		SpannableString allText = new SpannableString(upper + "\n" + lower);
		allText.setSpan(upperSpan, 0, upper.length(), 0);

		// Merge the spans together so we can set custom typeface
		allText.setSpan(lowerSpan, upper.length() + 1, allText.length(), 0);
		allText.setSpan(new FontCacheTypefaceSpan(FontCache.Font.ROBOTO_LIGHT), upper.length() + 1, allText.length(), 0);

		pin.setText(allText, TextView.BufferType.SPANNABLE);
	}

	private void setPinImage(TextView pin, int drawableId) {
		RoundBitmapDrawable d = new RoundBitmapDrawable(getActivity(), drawableId, mPinDimension);
		pin.setBackgroundDrawable(d);
	}

	private Point2D.Double transform(double lat, double lon) {
		Point2D.Double p = new Point2D.Double();
		mProjection.transform(lon, lat, p);
		p.y = mPicture.getHeight() - p.y;
		return p;
	}

	public static class RoundBitmapDrawable extends Drawable {
		private Bitmap mBitmap;
		private float mDimension;
		private Matrix mMatrix;
		private Paint mPaint;

		public RoundBitmapDrawable(Context context, int drawableId, float dimension) {
			mDimension = dimension;
			mBitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);

			BitmapShader shader;
			shader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setShader(shader);

			mMatrix = new Matrix();
			float padding = (mDimension - mBitmap.getWidth()) / 2;
			mMatrix.postTranslate(padding, padding);
		}

		@Override
		public void draw(Canvas canvas) {
			canvas.save();
			canvas.concat(mMatrix);
			canvas.drawCircle(mBitmap.getWidth()/2, mBitmap.getHeight()/2, mBitmap.getWidth()/2, mPaint);
			canvas.restore();
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSPARENT;
		}

		@Override
		public void setAlpha(int alpha) {
			mPaint.setAlpha(alpha);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			mPaint.setColorFilter(cf);
		}
	}
}

