package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
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
import com.mobiata.android.util.AndroidUtils;
import com.squareup.otto.Subscribe;

public class TabletLaunchMapFragment extends SvgMapFragment {
	private LayoutInflater mInflater;
	private FrameLayout mRoot;
	private FrameLayout mPinC;

	private SvgDrawable mMapDrawable;
	private Drawable mTiledDotDrawable;
	private GradientDrawable mLinearGradDrawable;
	private GradientDrawable mRadialGradDrawable;

	private LaunchCollection mSelectedCollection;
	private LaunchLocation mSelectedLocation;

	private List<Rect> mNonOverlappingRects = new ArrayList<Rect>();

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
	public void onLaunchCollectionsAvailable(final Events.LaunchCollectionsAvailable event) {
		mSelectedCollection = event.selectedCollection;
		mSelectedLocation = event.selectedLocation;
		onLaunchCollectionClicked(new Events.LaunchCollectionClicked(event.selectedCollection));
	}

	@Subscribe
	public void onLaunchCollectionClicked(final Events.LaunchCollectionClicked event) {
		if (isMeasurable()) {
			renderMap(event.launchCollection);
		}
		else {
			mRoot.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					mRoot.getViewTreeObserver().removeOnPreDrawListener(this);
					renderMap(event.launchCollection);
					return true;
				}
			});
		}
	}

	private LaunchPin findClickedPin() {
		for (int i = 0; i < mPinC.getChildCount(); i++) {
			LaunchPin pin = (LaunchPin) mPinC.getChildAt(i);
			if (pin.getLaunchLocation() == mSelectedLocation && pin.getVisibility() != View.GONE) {
				return pin;
			}
		}

		return null;
	}

	SingleStateListener mDetailsStateListener = new SingleStateListener<>(
		LaunchState.DEFAULT, LaunchState.DETAILS, true, new ISingleStateListener() {
		@Override
		public void onStateTransitionStart(boolean isReversed) {
			LaunchPin pin = findClickedPin();
			if (pin != null) {
				pin.setVisibility(View.INVISIBLE);
			}
			mPinC.setVisibility(View.VISIBLE);
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
				LaunchPin pin = findClickedPin();
				if (pin != null) {
					pin.setVisibility(View.VISIBLE);
				}
			}

			if (isReversed) {
				mPinC.setVisibility(View.VISIBLE);
			}
			else {
				mPinC.setVisibility(View.INVISIBLE);
			}
		}
	}
	);

	public Rect getClickedPinRect() {
		LaunchPin pin = findClickedPin();
		if (pin != null) {
			return pin.getPinGlobalPosition();
		}

		return null;
	}

	/*
	 * Private methods
	 */

	private void renderMap(LaunchCollection launchCollection) {
		if (launchCollection == null || launchCollection.locations == null) {
			return;
		}

		generateMap(launchCollection);
		generatePins(launchCollection);
	}

	private void init() {
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
		setPadding(otherPadding, otherPadding / 2 + abHeight, otherPadding, bottomPadding);

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
			mMapDrawable,
			mTiledDotDrawable,
			mLinearGradDrawable,
			mRadialGradDrawable,
		};
		LayerDrawable allDrawables = new LayerDrawable(drawables);
		getMapView().setBackgroundDrawable(allDrawables);
		int layerType = AndroidUtils.hasJellyBeanMR1() ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE;
		getMapView().setLayerType(layerType, null);
	}

	private void generatePins(LaunchCollection launchCollection) {
		mPinC.removeAllViews();
		mNonOverlappingRects.clear();
		for (LaunchLocation launchLocation : launchCollection.locations) {
			addPin(launchLocation);
		}
	}

	private void addPin(final LaunchLocation launchLocation) {
		final LaunchPin pin = Ui.inflate(mInflater, R.layout.snippet_tablet_launch_map_pin, mRoot, false);
		pin.bind(launchLocation);
		pin.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

		// Position on screen
		Location loc = launchLocation.location.getLocation();
		Point2D.Double transformed = projectToScreen(loc.getLatitude(), loc.getLongitude());
		int marginLeft = (int) (transformed.x - pin.getMeasuredWidth() / 2);
		int marginTop = (int) (transformed.y - pin.getMeasuredHeight() / 2);

		boolean overlaps = false;
		Rect thisPinRect = new Rect(marginLeft, marginTop, marginLeft + pin.getMeasuredWidth(), marginTop + pin.getMeasuredHeight());
		for (Rect otherPinRect : mNonOverlappingRects) {
			if (Rect.intersects(thisPinRect, otherPinRect)) {
				overlaps = true;
				break;
			}
		}

		if (!overlaps) {
			mNonOverlappingRects.add(thisPinRect);

			MarginLayoutParams lp = (MarginLayoutParams) pin.getLayoutParams();
			lp.setMargins(marginLeft, marginTop, 0, 0);
			pin.setLayoutParams(lp);

			mPinC.addView(pin);
			pin.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mSelectedLocation = launchLocation;
					Events.post(new Events.LaunchMapPinClicked(launchLocation));
				}
			});

			pin.retrieveImageAndStartAnimation();
		}
	}
}
