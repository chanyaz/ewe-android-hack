package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
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
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.graphics.RoundBitmapDrawable;
import com.expedia.bookings.graphics.SvgDrawable;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LaunchPin;
import com.jhlabs.map.Point2D;

public class TabletLaunchMapFragment extends SvgMapFragment {
	private LayoutInflater mInflater;
	private FrameLayout mRoot;

	private double[] mLatLngs;

	private ColorDrawable mBgColorDrawable;
	private Drawable mTiledDotDrawable;
	private GradientDrawable mLinearGradDrawable;

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

		mRoot.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (mRoot.getWidth() > 0) {
					mRoot.getViewTreeObserver().removeOnPreDrawListener(this);
					renderMap();
				}
				return true;
			}
		});

		return mRoot;
	}

	public void setLatLngs(double... latLngs) {
		mLatLngs = latLngs;
	}

	public void renderMap() {
		generateMap();
		generatePins();
	}

	private void init() {
		mBgColorDrawable = new ColorDrawable(Color.parseColor("#1b2747"));

		mTiledDotDrawable = getResources().getDrawable(R.drawable.tiled_dot);

		// Linear Gradient
		int[] linearGradColors = new int[] {
			Color.parseColor("#001b2747"),
			Color.parseColor("#98131c33"),
			Color.parseColor("#ff131c33"),
		};
		mLinearGradDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, linearGradColors);
	}

	private void generateMap() {
		int w = getMapView().getWidth();
		int h = getMapView().getHeight();

		int searchHeaderHeight = getResources().getDimensionPixelSize(R.dimen.tablet_search_header_height);
		int stackHeight = getResources().getDimensionPixelSize(R.dimen.destination_search_stack_height);
		int bottomPadding = stackHeight + searchHeaderHeight * 3;

		int otherPadding = getResources().getDimensionPixelSize(R.dimen.launch_pin_size);
		int abHeight = getActivity().getActionBar().getHeight();
		setPadding(otherPadding, otherPadding + abHeight, otherPadding, bottomPadding);
		// TODO grab lat lngs from destination data type global data store
		mLatLngs = new double[] {
			37.770715, -122.405033,
			41.893077, 12.481627,
			40.425519, -3.709366,
			25.797418, -80.226341,
			45.525592, -73.553681
		};
		setBounds(mLatLngs);

		// Draw scaled and translated map
		SvgDrawable mapDrawable = new SvgDrawable(getSvg(), getViewportMatrix());

		// Radial Gradient
		int[] radialGradColors = new int[] {
			Color.parseColor("#00000000"),
			Color.parseColor("#5a000000"),
		};

		GradientDrawable radialGradDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, radialGradColors);
		radialGradDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
		radialGradDrawable.setGradientCenter(0.5f, 0.5f);
		radialGradDrawable.setGradientRadius(Math.min(w, h) * 0.65f);

		Drawable[] drawables = new Drawable[] {
			mBgColorDrawable,
			mapDrawable,
			mTiledDotDrawable,
			mLinearGradDrawable,
			radialGradDrawable,
		};
		LayerDrawable allDrawables = new LayerDrawable(drawables);
		getMapView().setBackgroundDrawable(allDrawables);
		mRoot.setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}

	// TODO: this is all temporary
	private void generatePins() {
		// TODO use data from launch JSON
		addPin(37.770715, -122.405033, "San Francisco", "From $320", R.drawable.mappin_sanfrancisco);
		addPin(41.893077, 12.481627, "Rome", "From $240", R.drawable.mappin_rome);
		addPin(40.425519, -3.709366, "Madrid", "From $450", R.drawable.mappin_madrid);
		addPin(25.797418, -80.226341, "Miami", "From $140", R.drawable.mappin_miami);
		addPin(45.525592, -73.553681, "Montreal", "From $100", R.drawable.mappin_montreal);
	}

	// TODO: this is all temporary
	public void addPin(final double lat, final double lon, String name, String price, int drawableId) {
		LaunchLocation metadata = new LaunchLocation();
		metadata.title = name;
		metadata.description = price;
		metadata.id = "";
		metadata.imageCode = "";

		SuggestionV2 suggestion = new SuggestionV2();
		suggestion.setDisplayName(name);
		Location location = new Location();
		location.setLatitude(lat);
		location.setLongitude(lon);
		suggestion.setLocation(location);
		metadata.location = suggestion;

		metadata.drawableId = drawableId;
		addPin(metadata);
	}

	public void addPin(final LaunchLocation metadata) {
		final LaunchPin pin = Ui.inflate(mInflater, R.layout.snippet_tablet_launch_map_pin, mRoot, false);
		pin.bind(metadata);
		mRoot.addView(pin);

		pin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
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
