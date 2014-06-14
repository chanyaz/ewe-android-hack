package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LaunchCollection;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.graphics.SvgDrawable;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LaunchPin;
import com.jhlabs.map.Point2D;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

public class TabletLaunchMapFragment extends SvgMapFragment {
	private LayoutInflater mInflater;
	private FrameLayout mRoot;
	private FrameLayout mPinC;

	private ColorDrawable mBgColorDrawable;
	private SvgDrawable mMapDrawable;
	private Drawable mTiledDotDrawable;
	private GradientDrawable mLinearGradDrawable;
	private GradientDrawable mRadialGradDrawable;

	LaunchPin mClickedPin;

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRoot = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);
		mPinC = Ui.findView(mRoot, R.id.pin_container);

		TabletLaunchControllerFragment controller = (TabletLaunchControllerFragment) getParentFragment();
		controller.registerStateListener(mDetailsStateListener, false);

		return mRoot;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	/*
	 * Otto events
	 */

	@Subscribe
	public void onLaunchCollectionClicked(Events.LaunchCollectionClicked event) {
		// TODO animation
		renderMap(event.launchCollection);
	}

	@Subscribe
	public void onLaunchCollectionsAvailable(final Events.LaunchCollectionsAvailable event) {
		mRoot.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (mRoot.getWidth() > 0) {
					mRoot.getViewTreeObserver().removeOnPreDrawListener(this);
					if (event.collections != null && event.collections.size() > 0) {
						renderMap(event.collections.get(0));
					}
				}
				return true;
			}
		});
	}

	SingleStateListener mDetailsStateListener = new SingleStateListener<>(
		LaunchState.DEFAULT, LaunchState.DETAILS, true, new ISingleStateListener() {
		@Override
		public void onStateTransitionStart(boolean isReversed) {
			for (int i = 0; i < mPinC.getChildCount(); i++) {
				View child = mPinC.getChildAt(i);
				if (child instanceof LaunchPin) {
					LaunchPin pin = (LaunchPin) child;
					if (pin.equals(mClickedPin)) {
						pin.setVisibility(View.INVISIBLE);
					}
				}
			}
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			mPinC.setAlpha(1f - percentage);
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {

		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			if (isReversed) {
				for (int i = 0; i < mPinC.getChildCount(); i++) {
					View child = mPinC.getChildAt(i);
					child.setVisibility(View.VISIBLE);
				}
			}
		}
	}
	);

	/*
	 * Private methods
	 */

	private void renderMap(LaunchCollection launchCollection) {
		generateMap(launchCollection);
		generatePins(launchCollection);
	}

	private void init() {
		mBgColorDrawable = new ColorDrawable(getResources().getColor(R.color.tablet_launch_bg));

		mTiledDotDrawable = getResources().getDrawable(R.drawable.tiled_dot);

		// Linear Gradient
		int[] linearGradColors = new int[] {
			Color.parseColor("#001b2747"),
			Color.parseColor("#98131c33"),
			Color.parseColor("#ff131c33"),
		};
		mLinearGradDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, linearGradColors);

		// Radial Gradient
		int[] radialGradColors = new int[] {
			Color.parseColor("#00000000"),
			Color.parseColor("#5a000000"),
		};
		mRadialGradDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, radialGradColors);
		mRadialGradDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
		mRadialGradDrawable.setGradientCenter(0.5f, 0.5f);
	}

	private void generateMap(LaunchCollection launchCollection) {
		int w = getMapView().getWidth();
		int h = getMapView().getHeight();

		int searchHeaderHeight = getResources().getDimensionPixelSize(R.dimen.tablet_search_header_height);
		int stackHeight = getResources().getDimensionPixelSize(R.dimen.destination_search_stack_height);
		int bottomPadding = stackHeight + searchHeaderHeight * 3;

		int otherPadding = getResources().getDimensionPixelSize(R.dimen.launch_pin_size);
		int abHeight = getActivity().getActionBar().getHeight();
		setPadding(otherPadding, otherPadding + abHeight, otherPadding, bottomPadding);

		double[] latLngs = new double[launchCollection.locations.size() * 2];
		for (int i = 0; i < launchCollection.locations.size(); i++) {
			Location location = launchCollection.locations.get(i).location.getLocation();
			latLngs[2 * i] = location.getLatitude();
			latLngs[2 * i + 1] = location.getLongitude();
		}
		setBounds(latLngs);

		// Draw scaled and translated map
		mMapDrawable = new SvgDrawable(getSvg(), getViewportMatrix());

		mRadialGradDrawable.setGradientRadius(Math.min(w, h) * 0.65f);

		Drawable[] drawables = new Drawable[] {
			mBgColorDrawable,
			mMapDrawable,
			mTiledDotDrawable,
			mLinearGradDrawable,
			mRadialGradDrawable,
		};
		LayerDrawable allDrawables = new LayerDrawable(drawables);
		getMapView().setBackgroundDrawable(allDrawables);
		mRoot.setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}

	private void generatePins(LaunchCollection launchCollection) {
		mPinC.removeAllViews();
		for (LaunchLocation launchLocation : launchCollection.locations) {
			addPin(launchLocation);
		}
	}

	private void addPin(final LaunchLocation metadata) {
		final LaunchPin pin = Ui.inflate(mInflater, R.layout.snippet_tablet_launch_map_pin, mRoot, false);
		pin.bind(metadata);
		mPinC.addView(pin);

		pin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mClickedPin = pin;
				Events.post(new Events.LaunchMapPinClicked(pin.getGlobalOrigin(), metadata));
			}
		});

		pin.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				pin.getViewTreeObserver().removeOnPreDrawListener(this);

				// Position on screen now we know the pin dimensions
				Location loc = metadata.location.getLocation();
				Point2D.Double transformed = projectToScreen(loc.getLatitude(), loc.getLongitude());
				int marginLeft = (int) (transformed.x - pin.getWidth() / 2);
				int marginTop = (int) (transformed.y - pin.getHeight() / 2);
				MarginLayoutParams lp = (MarginLayoutParams) pin.getLayoutParams();
				lp.setMargins(marginLeft, marginTop, 0, 0);
				pin.setLayoutParams(lp);

				// Popin animation
				pin.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				pin.setPivotX(pin.getWidth() / 2.0f);
				float mapPinImageSize = getResources().getDimension(R.dimen.launch_pin_size);
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
}
