package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;

public class RoomTypeFragmentHandler extends RoomTypeHandler {
	private static final String PROPERTY_ROOM_CONTAINER_ID = "PROPERTY_ROOM_CONTAINER_ID";

	private View mRootView;

	public RoomTypeFragmentHandler(Context context, View rootView, Property property,
			SearchParams searchParams, Rate rate) {
		super(context, null, property, searchParams, rate, false);
		mRootView = rootView;
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle methods

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mRoomTypeRowContainer = mRootView.findViewById(((Integer) savedInstanceState
					.get(PROPERTY_ROOM_CONTAINER_ID)).intValue());
		}
	}

	@Override
	public void onResume() {
		// Do nothing
	}

	@Override
	public void onDestroy() {
		// Do nothing
	}

	public void saveToBundle(Bundle outState) {
		outState.putInt(PROPERTY_ROOM_CONTAINER_ID, mRoomTypeRowContainer.getId());
	}
	
	/**
	 * This method is responsible for updating the room type
	 * information as the rate changes
	 */
	public void updateRoomDetails(Rate rate) {
		mRate = rate;
		updateRoomTypeDescription();
		showCheckInCheckoutDetails();
	}

}
