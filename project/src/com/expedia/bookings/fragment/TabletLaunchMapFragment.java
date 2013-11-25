package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
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
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.graphics.RoundBitmapDrawable;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.SpannableBuilder;
import com.jhlabs.map.Point2D;
import com.mobiata.android.bitmaps.BitmapDrawable;

public class TabletLaunchMapFragment extends SvgMapFragment {
	private LayoutInflater mInflater;
	private FrameLayout mRoot;

	public static TabletLaunchMapFragment newInstance() {
		TabletLaunchMapFragment frag = new TabletLaunchMapFragment();
		frag.setMapResource(R.raw.map_tablet_launch);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mInflater = LayoutInflater.from(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRoot = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);

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

	private void generateMap() {
		int w = getMapImageView().getWidth();
		int h = getMapImageView().getHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		bitmap.eraseColor(Color.parseColor("#1b2747"));

		int bottomPadding = getResources().getDimensionPixelSize(R.dimen.destination_search_stack_height);
		int otherPadding = getResources().getDimensionPixelSize(R.dimen.tablet_launch_map_pin_image_size) / 2;
		int abHeight = getActivity().getActionBar().getHeight();
		setPadding(otherPadding, otherPadding + abHeight, otherPadding, bottomPadding);
		setBounds(
			37.770715, -122.405033,
			41.893077, 12.481627,
			40.425519, -3.709366,
			25.797418, -80.226341,
			45.525592, -73.553681
		);

		// Draw scaled and translated map
		Canvas c = new Canvas(bitmap);
		c.setMatrix(getViewportMatrix());
		getMapPicture().draw(c);
		c.setMatrix(new Matrix());

		// Dot grid
		Bitmap tiledBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tileable_dot_grid);
		BitmapDrawable tiled = new BitmapDrawable(tiledBitmap);
		tiled.setTileModeX(Shader.TileMode.REPEAT);
		tiled.setTileModeY(Shader.TileMode.REPEAT);
		tiled.setBounds(0, 0, w, h);
		tiled.draw(c);

		// Linear Gradient
		int[] linearGradColors = new int[] {
			Color.parseColor("#001b2747"),
			Color.parseColor("#98131c33"),
			Color.parseColor("#131c33"),
		};
		float [] linearGradPositions = new float[] {
			0.0f,
			0.5f,
			1.0f,
		};
		LinearGradient linearShader = new LinearGradient(w/2, 0, w/2, h, linearGradColors, linearGradPositions, Shader.TileMode.REPEAT);

		// Radial Gradient
		float radius = Math.min(w, h) * 0.65f;
		RadialGradient radialShader = new RadialGradient(w/2.0f, h/2.0f, radius, Color.parseColor("#00000000"), Color.parseColor("#5a000000"), Shader.TileMode.CLAMP);

		ComposeShader composeShader = new ComposeShader(linearShader, radialShader, PorterDuff.Mode.SRC_OVER);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setShader(composeShader);
		paint.setDither(true);
		c.drawRect(new RectF(0, 0, w, h), paint);

		getMapImageView().setImageDrawable(new BitmapDrawable(bitmap));

		// Cleanup
		tiled.setBitmap(null);
		tiledBitmap.recycle();
	}

	private void generatePins() {
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
				anim.setStartDelay(Math.max(0, 400 + marginLeft / 5));
				anim.start();
				return true;
			}
		});
	}

	private void setPinText(TextView pin, String upper, String lower) {
		TextAppearanceSpan upperSpan = new TextAppearanceSpan(getActivity(), R.style.MapPinUpperTextAppearance);
		TextAppearanceSpan lowerSpan = new TextAppearanceSpan(getActivity(), R.style.MapPinLowerTextAppearance);

		SpannableBuilder sb = new SpannableBuilder();
		sb.append(upper, upperSpan);
		sb.append("\n");
		sb.append(lower, lowerSpan, FontCache.getSpan(FontCache.Font.ROBOTO_LIGHT));

		pin.setText(sb.build(), TextView.BufferType.SPANNABLE);
	}

	private void setPinImage(TextView pin, int drawableId) {
		RoundBitmapDrawable d = new RoundBitmapDrawable(getActivity(), drawableId);
		pin.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
	}
}
