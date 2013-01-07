package com.expedia.bookings.maps;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;

/**
 * You can't do anything with maps (like animate cameras) until they
 * have been laid out.  As a result, this implements a way to tell
 * when the layout has occurred and you can manipulate the camera.
 */
public class SupportMapFragment extends com.google.android.gms.maps.SupportMapFragment {

	private boolean mLoaded = false;

	public static SupportMapFragment newInstance() {
		SupportMapFragment frag = new SupportMapFragment();
		return frag;
	}

	public class QueuedCameraUpdate {
		CameraUpdate cameraUpdate;
		boolean isAnimated;
	}

	private ArrayList<QueuedCameraUpdate> mUpdateQueue = new ArrayList<QueuedCameraUpdate>();

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
			mUpdateQueue.add(queuedUpdate);
		}
	}

	private void drainCameraUpdateQueue() {
		for (QueuedCameraUpdate update : mUpdateQueue) {
			changeCamera(update.cameraUpdate, update.isAnimated);
		}
		// We are done with the queue for good
		mUpdateQueue = null;
	}

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
		drainCameraUpdateQueue();
	}
}
