package com.expedia.bookings.fragment;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.bitmaps.UrlBitmapDrawable;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.maps.SupportMapFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
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
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

public class TabletLaunchMapFragment extends SupportMapFragment {
	private HashMap<LaunchLocation, Marker> mLocations;
	private LaunchLocation mClickedLocation;

	// value taken from google-play-services.jar
	private static final String MAP_OPTIONS = "MapOptions";

	public static TabletLaunchMapFragment newInstance() {
		TabletLaunchMapFragment frag = new TabletLaunchMapFragment();

		int mapType = GoogleMap.MAP_TYPE_SATELLITE;

		if (ExpediaBookingApp.IS_AUTOMATION) {
			mapType = GoogleMap.MAP_TYPE_NONE;
		}

		GoogleMapOptions options = new GoogleMapOptions();
		options.mapType(mapType)
			.camera(CameraPosition.fromLatLngZoom(new LatLng(0, 0), 1f))
			.zoomControlsEnabled(false)
			.zoomGesturesEnabled(true);

		Bundle args = new Bundle();
		args.putParcelable(MAP_OPTIONS, options);
		frag.setArguments(args);

		return frag;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		TabletLaunchControllerFragment controller = (TabletLaunchControllerFragment) getParentFragment();

		controller.registerStateListener(mDetailsStateListener, false);
		controller.registerStateListener(mWaypointStateListener, false);

		getMap().setOnMarkerClickListener(mMarkerClickListener);

		adjustMapPadding(true);

		addOverlay();
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
		replaceAllPins(event.selectedCollection.locations);
	}

	@Subscribe
	public void onLaunchCollectionClicked(final Events.LaunchCollectionClicked event) {
		replaceAllPins(event.launchCollection.locations);
	}

	/*
	 * State Listeners
	 */

	SingleStateListener mDetailsStateListener = new SingleStateListener<>(
		LaunchState.DEFAULT, LaunchState.DETAILS, true, new ISingleStateListener() {
		@Override
		public void onStateTransitionStart(boolean isReversed) {
			if (!isReversed) {
				adjustMapPadding(false);
			}
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			for (LaunchLocation location : mLocations.keySet()) {
				Marker marker = mLocations.get(location);
				if (mClickedLocation != null && mClickedLocation.equals(location)) {
					marker.setAlpha(!isReversed || percentage < 1f ? 0f : 1f);
				}
				else {
					marker.setAlpha(1f - percentage);
				}
			}
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {
			if (mClickedLocation != null && isReversed) {
				mLocations.get(mClickedLocation).setAlpha(1f);
			}
		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			if (isReversed) {
				adjustMapPadding(true);
			}
		}
	}
	);

	SingleStateListener mWaypointStateListener = new SingleStateListener<>(
		LaunchState.DEFAULT, LaunchState.WAYPOINT, true, new ISingleStateListener() {
		@Override
		public void onStateTransitionStart(boolean isReversed) {
			if (!isReversed) {
				adjustMapPadding(false);
			}
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			for (Marker marker : mLocations.values()) {
				if (marker != null) {
					marker.setAlpha(1f - percentage);
				}
			}
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {

		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			if (isReversed) {
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
		return mLocations == null || mClickedLocation == null ? null : getPinRect(mLocations.get(mClickedLocation));
	}

	/*
	 * Private methods
	 */

	private void adjustMapPadding(boolean withStacksVisible) {
		int stackHeight = getResources().getDimensionPixelSize(R.dimen.destination_search_stack_height);
		int bottomPadding = withStacksVisible ? stackHeight : 0;
		int abHeight = getActivity().getActionBar().getHeight();
		setPadding(0, abHeight, 0, bottomPadding);
	}

	private void animateCameraToShowFullCollection() {
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
		if (mLocations == null) {
			mLocations = new HashMap<>();
		}
		else {
			for (LaunchLocation location : mLocations.keySet()) {
				mLocations.get(location).remove();
			}
			mLocations.clear();
		}

		for (LaunchLocation location : locations) {
			mLocations.put(location, null);
			addPin(location);
		}

		animateCameraToShowFullCollection();
	}

	private void addPin(final LaunchLocation launchLocation) {
		inflatePinAndAddMarker(launchLocation, null);

		UrlBitmapDrawable bitmap = new UrlBitmapDrawable(getResources(), launchLocation.getImageUrl());
		bitmap.setOnBitmapLoadedCallback(new L2ImageCache.OnBitmapLoaded() {
			@Override
			public void onBitmapLoaded(String url, Bitmap bitmap) {
				inflatePinAndAddMarker(launchLocation, bitmap);
			}

			@Override
			public void onBitmapLoadFailed(String url) {
			}
		});
	}

	private Marker inflatePinAndAddMarker(LaunchLocation launchLocation, Bitmap bitmap) {
		// Create a detached LaunchPin view
		final LaunchPin pin = Ui.inflate(LayoutInflater.from(getActivity()), R.layout.snippet_tablet_launch_map_pin, null, false);
		pin.bind(launchLocation);
		pin.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		pin.layout(0, 0, pin.getMeasuredWidth(), pin.getMeasuredHeight());
		pin.setPinBitmap(bitmap);
		Bitmap pinBitmap = Ui.createBitmapFromView(pin);

		Location location = launchLocation.location.getLocation();
		MarkerOptions options = new MarkerOptions()
			.position(new LatLng(location.getLatitude(), location.getLongitude()))
			.icon(BitmapDescriptorFactory.fromBitmap(pinBitmap))
			.anchor(0.5f, getResources().getDimension(R.dimen.launch_pin_size) / 2 / pin.getMeasuredHeight())
			.title(launchLocation.id)
			.alpha(0f);
		Marker marker = getMap().addMarker(options);

		// Fade this marker in
		ObjectAnimator anim = ObjectAnimator.ofFloat(marker, "alpha", 1f);

		// Remove the existing marker
		if (mLocations.get(launchLocation) != null) {
			final Marker oldMarker = mLocations.get(launchLocation);
			anim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					oldMarker.remove();
					oldMarker.setVisible(false);
				}
			});
		}

		Rect pinRect = getPinRect(marker);
		anim.setDuration(500);
		anim.setStartDelay(Math.max(0, 400 + pinRect.left / 5));

		anim.start();

		mLocations.put(launchLocation, marker);
		return marker;
	}

	private GoogleMap.OnMarkerClickListener mMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
		@Override
		public boolean onMarkerClick(Marker marker) {
			mClickedLocation = null;
			for (LaunchLocation location : mLocations.keySet()) {
				Marker m = mLocations.get(location);
				if (m != null && m.getTitle().equals(marker.getTitle())) {
					mClickedLocation = location;
					Events.post(new Events.LaunchMapPinClicked(mClickedLocation));
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
}
