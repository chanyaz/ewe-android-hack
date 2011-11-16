package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.PropertyInfo;
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.mobiata.android.json.JSONUtils;

public class RoomTypeFragmentHandler extends RoomTypeHandler {
	private static final String PROPERTY_ROOM_CONTAINER_ID = "PROPERTY_ROOM_CONTAINER_ID";
	private static final String INSTANCE_EXPANDED = "INSTANCE_EXPANDED";

	private View mRootView;

	public RoomTypeFragmentHandler(Context context, View rootView, Property property,
			SearchParams searchParams, Rate rate) {
		super(context, null, property, searchParams, rate);
		mRootView = rootView;
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle methods

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mPropertyInfo = (PropertyInfo) JSONUtils.parseJSONObjectFromBundle(savedInstanceState, Codes.PROPERTY_INFO,
					PropertyInfo.class);
			mRoomTypeRowContainer = mRootView.findViewById(((Integer) savedInstanceState
					.get(PROPERTY_ROOM_CONTAINER_ID)).intValue());
			if ((Boolean) savedInstanceState.get(INSTANCE_EXPANDED)) {
				setVisibility(View.VISIBLE);
			}
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
		outState.putBoolean(INSTANCE_EXPANDED, isExpanded());
		outState.putInt(PROPERTY_ROOM_CONTAINER_ID, mRoomTypeRowContainer.getId());
		if (mPropertyInfo != null) {
			outState.putString(Codes.PROPERTY_INFO, mPropertyInfo.toJson().toString());
		}
	}

	public void loadDetails(PropertyInfoResponse response, String status) {
		if (mPropertyInfo == null) {
			if (response == null) {
				if (status != null) {
					showDetails(status);
					showCheckInCheckoutDetails(null);
				}
			}
			else {
				onPropertyInfoDownloaded(response);
			}
		}

		if (mPropertyInfo != null && mPropertyInfo.getPropertyId().equals(mProperty.getPropertyId())) {
			showDetails(mPropertyInfo);
			showCheckInCheckoutDetails(mPropertyInfo);
		}
	}
	
	/**
	 * This method is responsible for updating the room type
	 * information as the rate changes
	 */
	public void updateRoomDetails(Rate rate, PropertyInfoResponse response, String status) {
		mRate = rate;
		updateRoomTypeDescription();
		loadDetails(response, status);
	}

}
