package com.expedia.bookings.launch.fragment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.fragment.SupportMapFragment;
import com.expedia.bookings.launch.data.LaunchLocation;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.LaunchPin;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

public class TabletLaunchMapFragment extends SupportMapFragment {
	private HashMap<LaunchLocation, Marker> mLocations = new HashMap<>();

	// Store the id of the "clicked" location (i.e. the one expanded to show details)
	private String mClickedLocation;

	// The alpha ceiling for map markers. If we're viewing details or waypoint selection,
	// this will be 0f, and if it's in transition, it will be somewhere in between.
	float mMarkerAlpha = 1f;

	// value taken from google-play-services.jar
	private static final String MAP_OPTIONS = "MapOptions";

	public static TabletLaunchMapFragment newInstance() {
		TabletLaunchMapFragment frag = new TabletLaunchMapFragment();

		int mapType = GoogleMap.MAP_TYPE_SATELLITE;

		if (ExpediaBookingApp.isAutomation()) {
			mapType = GoogleMap.MAP_TYPE_NONE;
		}

		GoogleMapOptions options = new GoogleMapOptions();
		CameraPosition camera = CameraPosition.builder()
			.target(new LatLng(20, -40))
			.zoom(1.0f)
			.tilt(45.0f)
			.build();
		options.mapType(mapType)
			.compassEnabled(false)
			.camera(camera)
			.zoomControlsEnabled(false)
			.zoomGesturesEnabled(true);

		Bundle args = new Bundle();
		args.putParcelable(MAP_OPTIONS, options);
		frag.setArguments(args);

		return frag;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		getMap().setOnMarkerClickListener(mMarkerClickListener);

		adjustMapPadding(true);

		addOverlay();
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);

		TabletLaunchControllerFragment controller = (TabletLaunchControllerFragment) getParentFragment();
		controller.registerStateListener(mDetailsStateListener, false);
		controller.registerStateListener(mWaypointStateListener, false);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);

		TabletLaunchControllerFragment controller = (TabletLaunchControllerFragment) getParentFragment();
		controller.unRegisterStateListener(mDetailsStateListener);
		controller.unRegisterStateListener(mWaypointStateListener);
	}

	/*
	 * Otto events
	 */

	@Subscribe
	public void onLaunchCollectionsAvailable(final Events.LaunchCollectionsAvailable event) {
		if (event.selectedCollection != null) {
			replaceAllPins(event.selectedCollection.locations);
		}
	}

	@Subscribe
	public void onLaunchCollectionClicked(final Events.LaunchCollectionClicked event) {
		if (event.launchCollection != null) {
			replaceAllPins(event.launchCollection.locations);
		}
	}

	/*
	 * State Listeners
	 */

	SingleStateListener mDetailsStateListener = new SingleStateListener<>(
		LaunchState.OVERVIEW, LaunchState.DETAILS, true, new ISingleStateListener() {
		@Override
		public void onStateTransitionStart(boolean isReversed) {
			mMarkerAlpha = isReversed ? 0f : 1f;
			if (!isReversed) {
				adjustMapPadding(false);
			}
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			mMarkerAlpha = 1f - percentage;
			for (LaunchLocation location : mLocations.keySet()) {
				Marker marker = mLocations.get(location);
				if (mClickedLocation != null && mClickedLocation.equals(location.id)) {
					marker.setAlpha(!isReversed || percentage < 1f ? 0f : 1f);
				}
				else {
					marker.setAlpha(mMarkerAlpha);
				}
			}
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {
			mMarkerAlpha = isReversed ? 1f : 0f;
			if (mClickedLocation != null && isReversed) {
				for (Marker marker : mLocations.values()) {
					marker.setAlpha(mMarkerAlpha);
				}
			}
		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			mMarkerAlpha = isReversed ? 1f : 0f;
			if (isAdded() && isReversed) {
				adjustMapPadding(true);
			}
		}
	}
	);

	SingleStateListener mWaypointStateListener = new SingleStateListener<>(
		LaunchState.OVERVIEW, LaunchState.WAYPOINT, true, new ISingleStateListener() {
		@Override
		public void onStateTransitionStart(boolean isReversed) {
			mMarkerAlpha = isReversed ? 0f : 1f;
			if (!isReversed) {
				adjustMapPadding(false);
			}
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			mMarkerAlpha = 1f - percentage;
			for (Marker marker : mLocations.values()) {
				if (marker != null) {
					marker.setAlpha(mMarkerAlpha);
				}
			}
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {
			mMarkerAlpha = isReversed ? 1f : 0f;
		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			mMarkerAlpha = isReversed ? 1f : 0f;
			if (isAdded() && isReversed) {
				adjustMapPadding(true);
			}
		}
	}
	);

	/*
	 * Public methods
	 */

	public Rect getPinRect(Marker marker) {
		if (marker == null) {
			return null;
		}
		Projection projection = getMap().getProjection();

		// Projection of the marker on the view
		Point markerPoint = projection.toScreenLocation(marker.getPosition());

		// Offset by location of this fragment on the screen
		View view = getView();
		int[] offsets = new int[2];
		view.getLocationOnScreen(offsets);
		markerPoint.offset(offsets[0], offsets[1]);

		float size = getResources().getDimension(R.dimen.launch_pin_size) / 2f;
		Rect pinRect = new Rect(
			(int) (markerPoint.x - size),
			(int) (markerPoint.y - size),
			(int) (markerPoint.x + size),
			(int) (markerPoint.y + size)
		);
		return pinRect;
	}

	public Rect getClickedPinRect() {
		if (mClickedLocation != null) {
			for (LaunchLocation location : mLocations.keySet()) {
				if (mClickedLocation.equals(location.id)) {
					Marker marker = mLocations.get(location);
					return getPinRect(marker);
				}
			}
		}

		return null;
	}

	/*
	 * Private methods
	 */

	private void adjustMapPadding(boolean withStacksVisible) {
		int stackHeight = getResources().getDimensionPixelSize(R.dimen.destination_search_stack_height);
		int navBarPadding = getResources().getDimensionPixelOffset(R.dimen.extra_navigation_bar_padding);
		int bottomPadding = withStacksVisible ? stackHeight + navBarPadding : navBarPadding;
		int abHeight = getActivity().getActionBar().getHeight();
		setPadding(0, abHeight, 0, bottomPadding);
	}

	private void animateCameraToShowFullCollection() {
		if (mLocations.size() == 0) {
			return;
		}
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (LaunchLocation location : mLocations.keySet()) {
			LatLng latlng = new LatLng(location.location.getLocation().getLatitude(), location.location.getLocation().getLongitude());
			builder.include(latlng);
		}
		LatLngBounds bounds = builder.build();
		int padding = 0; // offset from edges of the map in pixels
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
		animateCamera(cu);
	}

	private void replaceAllPins(List<LaunchLocation> locations) {
		for (Marker marker : mLocations.values()) {
			marker.remove();
		}
		mLocations.clear();

		for (LaunchLocation location : locations) {
			addPin(location);
		}

		animateCameraToShowFullCollection();
	}

	private class PinCallback extends PicassoTarget {
		private LaunchLocation mLaunchLocation;

		public PinCallback(LaunchLocation launchLocation) {
				mLaunchLocation = launchLocation;
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			if (getActivity() != null) {
				super.onBitmapLoaded(bitmap, from);
				inflatePinAndAddMarker(mLaunchLocation, bitmap);
			}
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			super.onBitmapFailed(errorDrawable);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			if (getActivity() != null) {
				super.onPrepareLoad(placeHolderDrawable);
				inflatePinAndAddMarker(mLaunchLocation, ((BitmapDrawable) placeHolderDrawable).getBitmap());
			}
		}
	}


	private ArrayList<PinCallback> targetList = new ArrayList<PinCallback>();
	private void addPin(final LaunchLocation launchLocation) {
		if (getActivity() != null) {
			final String imageUrl = TabletLaunchPinDetailFragment.getResizedImageUrl(getActivity(), launchLocation);

			// Immediately inflate a pin with whatever we have cached (might be null)
			PinCallback target = new PinCallback(launchLocation);
			targetList.add(target);
			new PicassoHelper.Builder(getActivity())
				.setPlaceholder(R.drawable.launch_circle_placeholder)
				.setTarget(target).build().load(imageUrl);

		}
	}

	private void inflatePinAndAddMarker(final LaunchLocation launchLocation, Bitmap bitmap) {
		Bitmap pinBitmap = LaunchPin.createViewBitmap(getActivity(), launchLocation, bitmap);

		MarkerOptions options = new MarkerOptions()
			.position(getLatLng(launchLocation))
			.icon(BitmapDescriptorFactory.fromBitmap(pinBitmap))
			.anchor(0.5f, getResources().getDimension(R.dimen.launch_pin_size) / 2 / pinBitmap.getHeight())
			.title(launchLocation.id)
			.alpha(0f);

		final Marker marker = getMap().addMarker(options);

		// Add animation effects, if the markers are not already transitioning.
		if (mMarkerAlpha == 1f) {
			ObjectAnimator anim = ObjectAnimator.ofFloat(marker, "alpha", 1f);

			anim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					finalizeAddMarker(launchLocation, marker);
				}
			});

			Rect pinRect = getPinRect(marker);
			anim.setDuration(500);
			anim.setStartDelay(Math.max(0, 400 + pinRect.left / 5));

			anim.start();
		}
		else {
			finalizeAddMarker(launchLocation, marker);
		}
	}

	private void finalizeAddMarker(LaunchLocation launchLocation, Marker marker) {
		marker.setAlpha(mMarkerAlpha);
		Marker oldMarker = mLocations.get(launchLocation);
		if (oldMarker != null) {
			oldMarker.remove();
			oldMarker.setVisible(false);
		}
		mLocations.put(launchLocation, marker);
	}

	private GoogleMap.OnMarkerClickListener mMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
		@Override
		public boolean onMarkerClick(Marker marker) {
			mClickedLocation = null;
			for (LaunchLocation location : mLocations.keySet()) {
				if (TextUtils.equals(mLocations.get(location).getTitle(), marker.getTitle())) {
					mClickedLocation = location.id;
					OmnitureTracking.trackLaunchCitySelect(mClickedLocation);
					Events.post(new Events.LaunchMapPinClicked(location));
					return true;
				}
			}
			return false;
		}
	};

	// Adds a dark blue transparent overlay on top of the map tiles (but under the pins)

	private void addOverlay() {
		TileOverlayOptions opts = new TileOverlayOptions()
			.tileProvider(mOverlayProvider);
		getMap().addTileOverlay(opts);
	}

	private TileProvider mOverlayProvider = new TileProvider() {
		private static final int SIZE = 1;
		private byte[] mBytes = null;

		@Override
		public Tile getTile(int x, int y, int zoom) {
			if (mBytes == null) {
				Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
				Paint paint = new Paint();
				paint.setColor(getResources().getColor(R.color.tablet_launch_map_overlay));
				Canvas canvas = new Canvas(bitmap);
				canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
				mBytes = stream.toByteArray();
			}

			return new Tile(SIZE, SIZE, mBytes);
		}
	};

	// Helper functions

	private LatLng getLatLng(LaunchLocation launchLocation) {
		Location location = launchLocation.location.getLocation();
		return new LatLng(location.getLatitude(), location.getLongitude());
	}

}
