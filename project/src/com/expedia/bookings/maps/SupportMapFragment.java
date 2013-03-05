package com.expedia.bookings.maps;

import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

/**
 * You can't do anything with maps (like animate cameras) until they
 * have been laid out.  As a result, this implements a way to tell
 * when the layout has occurred and you can manipulate the camera.
 * 
 * Also has some utilities that are useful overall.
 */
public class SupportMapFragment extends com.google.android.gms.maps.SupportMapFragment {

	private boolean mLoaded = false;

	private float mCenterOffsetX = 0;
	private float mCenterOffsetY = 0;

	public static SupportMapFragment newInstance() {
		SupportMapFragment frag = new SupportMapFragment();
		return frag;
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (view.getHeight() > 0) {
					view.getViewTreeObserver().removeGlobalOnLayoutListener(this);

					onMapLayout();
				}
			}
		});
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

	private void changeCamera(CameraUpdate cameraUpdate, boolean animate) {
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

	//////////////////////////////////////////////////////////////////////////
	// Center offset utilities
	//
	// Possible TODO: Handle zoom changes (not sure if possible)
	//
	// Possible TODO: Save center offset x/y on instance state?  Not sure if
	// useful (since changing instance state usually means config change, thus
	// the offsets may change).

	public void setCenterOffset(float x, float y) {
		mCenterOffsetX = x;
		mCenterOffsetY = y;
	}

	/**
	 * Offsets a lat lng by the amount of pixels desired.
	 * 
	 * *DOES NOT WORK* if you are changing the zoom level at the same time as changing the LatLng.
	 */
	public LatLng offsetLatLng(LatLng latLng) {
		if (mCenterOffsetX == 0 && mCenterOffsetY == 0) {
			return latLng;
		}

		Projection projection = getMap().getProjection();
		Point screenLocation = projection.toScreenLocation(latLng);
		screenLocation.x += mCenterOffsetX;
		screenLocation.y += mCenterOffsetY;
		return projection.fromScreenLocation(screenLocation);
	}

	public LatLng offsetLatLng(double latitude, double longitude) {
		return offsetLatLng(new LatLng(latitude, longitude));
	}
}
