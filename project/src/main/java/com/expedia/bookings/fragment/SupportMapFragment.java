package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.expedia.bookings.fragment.base.Measurable;
import com.expedia.bookings.fragment.base.MeasurableFragmentHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

/**
 * You can't do anything with maps (like animate cameras) until they
 * have been laid out.  As a result, this implements a way to tell
 * when the layout has occurred and you can manipulate the camera.
 *
 * Also has some utilities that are useful overall.
 */
public class SupportMapFragment extends com.google.android.gms.maps.SupportMapFragment implements Measurable {

	private SupportMapFragmentListener mListener;

	private boolean mLoaded = false;

	private float mCenterOffsetX = 0;
	private float mCenterOffsetY = 0;

	private int mWidth;
	private int mHeight;

	private MeasurableFragmentHelper mHelper;

	public static SupportMapFragment newInstance() {
		return new SupportMapFragment();
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		mListener = Ui.findFragmentListener(this, SupportMapFragmentListener.class, false);

		mHelper = new MeasurableFragmentHelper(this);
		mHelper.onAttach();
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Activity activity = getActivity();
				if (activity == null) {
					//Sometimes if the fragment attaches and then detaches quickly, activity will be null by this point.
					view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					return;
				}

				// We want to keep the width and height up to date so we don't remove this listener
				mWidth = view.getWidth();
				mHeight = view.getHeight();
				Log.v("SupportMapFragment global layout height=" + mHeight + " width=" + mWidth);
			}
		});

		view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Activity activity = getActivity();
				if (activity == null) {
					//Sometimes if the fragment attaches and then detaches quickly, activity will be null by this point.
					view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					return;
				}

				final int width = view.getWidth();
				final int height = view.getHeight();

				// https://code.google.com/p/gmaps-api-issues/issues/detail?id=4773
				// Someone commented saying that the map needs to be at least 200dp by 200dp
				final int minSize = (int) (200 * activity.getResources().getDisplayMetrics().density);
				if (height > minSize && width > minSize) {
					// Now that we've determined the map is large enough to touch we can remove the listener
					view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					onMapLayout();
				}
			}
		});

		mHelper.onViewCreated(view);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mHelper.onDestroyView();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mHelper.onDetach();
	}

	protected void onMapLayout() {
		mLoaded = true;
		if (mSavedInitCameraPosition != null) {
			changeCamera(mSavedInitCameraPosition.cameraUpdate, mSavedInitCameraPosition.isAnimated);
			mSavedInitCameraPosition = null;
		}

		if (mSavedCameraUpdate != null) {
			changeCamera(mSavedCameraUpdate.cameraUpdate, mSavedCameraUpdate.isAnimated);
			mSavedCameraUpdate = null;
		}

		if (mListener != null) {
			mListener.onMapLayout();
		}
	}

	public boolean isReady() {
		return mLoaded;
	}

	////////////////////////////////////////////////////////////////////////
	// General utilities

	public static LatLngBounds getAmericaBounds() {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		builder.include(new LatLng(50.513427, -125.529297));
		builder.include(new LatLng(25.085599, -63.984375));
		return builder.build();
	}

	/**
	 * @return true if this LatLng represents a point; false if it is null or (0, 0)
	 */
	public static boolean isValidLatLng(LatLng latLng) {
		return latLng != null && (latLng.latitude != 0 || latLng.longitude != 0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Camera utilities

	public class QueuedCameraUpdate {
		CameraUpdate cameraUpdate;
		boolean isAnimated;
	}

	private QueuedCameraUpdate mSavedInitCameraPosition;
	private QueuedCameraUpdate mSavedCameraUpdate;

	public void setInitialCameraPosition(CameraUpdate cameraUpdate) {
		if (mLoaded) {
			moveCamera(cameraUpdate);
		}
		else {
			mSavedInitCameraPosition = new QueuedCameraUpdate();
			mSavedInitCameraPosition.cameraUpdate = cameraUpdate;
			mSavedInitCameraPosition.isAnimated = false;
		}
	}

	public void animateCamera(CameraUpdate cameraUpdate) {
		changeCamera(cameraUpdate, true);
	}

	public void moveCamera(CameraUpdate cameraUpdate) {
		changeCamera(cameraUpdate, false);
	}

	public void changeCamera(CameraUpdate cameraUpdate, boolean animate) {
		GoogleMap map = getMap();

		if (mLoaded) {
			if (animate) {
				map.animateCamera(cameraUpdate);
			}
			else {
				map.moveCamera(cameraUpdate);
			}
		}
		else {
			QueuedCameraUpdate queuedUpdate = new QueuedCameraUpdate();
			queuedUpdate.cameraUpdate = cameraUpdate;
			queuedUpdate.isAnimated = animate;
			mSavedCameraUpdate = queuedUpdate;
		}
	}

	/**
	 * Equals with floats are bad.  This gets you close enough for government work.
	 */
	public boolean practicallyEquals(CameraPosition pos1, CameraPosition pos2) {
		return Math.abs(pos1.bearing - pos2.bearing) < .05
				&& Math.abs(pos1.tilt - pos2.tilt) < .05
				&& Math.abs(pos1.zoom - pos2.zoom) < .05
				&& Math.abs(pos1.target.latitude - pos2.target.latitude) < .0005f
				&& Math.abs(pos1.target.longitude - pos2.target.longitude) < .0005f;
	}

	//////////////////////////////////////////////////////////////////////////
	// Center offset utilities
	//
	// Possible TODO: Handle zoom changes (not sure if possible)
	//
	// Possible TODO: Save center offset x/y on instance state?  Not sure if
	// useful (since changing instance state usually means config change, thus
	// the offsets may change).

	public float getCenterOffsetX() {
		return mCenterOffsetX;
	}

	public float getCenterOffsety() {
		return mCenterOffsetY;
	}

	/**
	 * Offsets a lat lng by the amount of pixels desired.
	 *
	 * *DOES NOT WORK* if you are changing the zoom level at the same time as changing the LatLng.
	 */
	public LatLng offsetLatLng(LatLng latLng) {
		return offsetLatLng(latLng, mCenterOffsetX, mCenterOffsetY);
	}

	public LatLng offsetLatLng(LatLng latLng, float offsetX, float offsetY) {
		return offsetLatLng(latLng, offsetX, offsetY, getMap().getCameraPosition().zoom);
	}

	// KNOWN PROBLEM: Causes screen to flicker if the zoom changes from the current zoom
	public LatLng offsetLatLng(LatLng latLng, float offsetX, float offsetY, float zoom) {
		if (offsetX == 0 && offsetY == 0) {
			return latLng;
		}

		CameraPosition currCamPos = getMap().getCameraPosition();
		boolean changingZoomLevel = Math.abs(currCamPos.zoom - zoom) > .05;

		if (changingZoomLevel) {
			moveCamera(CameraUpdateFactory.zoomTo(zoom));
		}

		Projection projection = getMap().getProjection();
		Point screenLocation = projection.toScreenLocation(latLng);
		screenLocation.x += offsetX;
		screenLocation.y += offsetY;
		LatLng offsetLatLng = projection.fromScreenLocation(screenLocation);

		if (changingZoomLevel) {
			moveCamera(CameraUpdateFactory.newCameraPosition(currCamPos));
		}

		return offsetLatLng;
	}

	public void setPadding(int left, int top, int right, int bottom) {
		GoogleMap map = getMap();
		if (map != null) {
			map.setPadding(left, top, right, bottom);
		}
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	//////////////////////////////////////////////////////////////////////////
	// Measurable

	@Override
	public boolean isMeasurable() {
		return mHelper.isMeasurable();
	}

	//////////////////////////////////////////////////////////////////////////
	// Interface

	public interface SupportMapFragmentListener {
		void onMapLayout();
	}
}
