package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;

import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;

public class RoomTypeActivityHandler extends RoomTypeHandler {
	private Activity mActivity;

	public RoomTypeActivityHandler(Activity activity, Intent intent, Property property, SearchParams searchParams,
			Rate rate) {
		super(activity.getApplicationContext(), intent, property, searchParams, rate);
		mActivity = activity;
	}

	private static final int INSTANCE_EXPANDED = 102;
	private static final int PROPERTY_ROOM_CONTAINER_ID = 103;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle methods

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Intent intent = mActivity.getIntent();

		SparseArray<Object> instance = (SparseArray<Object>) mActivity.getLastNonConfigurationInstance();
		if (instance != null) {
			mRoomTypeRowContainer = mActivity.findViewById(((Integer) instance.get(PROPERTY_ROOM_CONTAINER_ID))
					.intValue());

			if ((Boolean) instance.get(INSTANCE_EXPANDED)) {
				setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onDestroy() {
		// Do nothing
	}

	@Override
	public void onResume() {
		loadDetails();
	}

	public void onRetainNonConfigurationInstance(SparseArray<Object> instance) {
		if (mRoomTypeRowContainer != null) {
			instance.put(PROPERTY_ROOM_CONTAINER_ID, new Integer(mRoomTypeRowContainer.getId()));
		}
		instance.put(INSTANCE_EXPANDED, isExpanded());
	}

	// Should be called to save PropertyInfo to any Intent
	public void saveToIntent(Intent intent) {
		// Do nothing?
	}

	public void loadDetails() {
		showDetails();
		showCheckInCheckoutDetails();
	}
}
