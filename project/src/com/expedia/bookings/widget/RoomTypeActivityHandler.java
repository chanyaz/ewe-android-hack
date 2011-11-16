package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;

import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.PropertyInfo;
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.PropertyInfoResponseHandler;
import com.expedia.bookings.tracking.TrackingUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.json.JSONUtils;

public class RoomTypeActivityHandler extends RoomTypeHandler implements Download, OnDownloadComplete {
	private Activity mActivity;

	public RoomTypeActivityHandler(Activity activity, Intent intent, Property property, SearchParams searchParams,
			Rate rate) {
		super(activity.getApplicationContext(), intent, property, searchParams, rate);
		mActivity = activity;
	}

	private static final int INSTANCE_PROPERTY_INFO = 101;
	private static final int INSTANCE_EXPANDED = 102;
	private static final int PROPERTY_ROOM_CONTAINER_ID = 103;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle methods

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Intent intent = mActivity.getIntent();
		mPropertyInfo = (PropertyInfo) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY_INFO,
				PropertyInfo.class);

		SparseArray<Object> instance = (SparseArray<Object>) mActivity.getLastNonConfigurationInstance();
		if (instance != null) {
			if (mPropertyInfo == null) {
				mPropertyInfo = (PropertyInfo) instance.get(INSTANCE_PROPERTY_INFO);
			}

			mRoomTypeRowContainer = mActivity.findViewById(((Integer) instance.get(PROPERTY_ROOM_CONTAINER_ID))
					.intValue());

			if ((Boolean) instance.get(INSTANCE_EXPANDED)) {
				setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onDestroy() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		String downloadKey = PropertyInfoResponseHandler.DOWNLOAD_KEY_PREFIX + mProperty.getPropertyId();
		if (bd.isDownloading(downloadKey)) {
			if (mActivity.isFinishing()) {
				bd.unregisterDownloadCallback(downloadKey, this);
			}
		}
	}

	@Override
	public void onResume() {
		loadDetails();
	}

	public void onRetainNonConfigurationInstance(SparseArray<Object> instance) {
		if (mPropertyInfo != null) {
			instance.put(INSTANCE_PROPERTY_INFO, mPropertyInfo);
		}
		if (mRoomTypeRowContainer != null) {
			instance.put(PROPERTY_ROOM_CONTAINER_ID, new Integer(mRoomTypeRowContainer.getId()));
		}
		instance.put(INSTANCE_EXPANDED, isExpanded());
	}

	// Should be called to save PropertyInfo to any Intent
	public void saveToIntent(Intent intent) {
		if (mPropertyInfo != null) {
			intent.putExtra(Codes.PROPERTY_INFO, mPropertyInfo.toJson().toString());
		}
	}

	public void loadDetails() {
		if (mPropertyInfo != null && mPropertyInfo.getPropertyId().equals(mProperty.getPropertyId())) {
			showDetails(mPropertyInfo);
			showCheckInCheckoutDetails(mPropertyInfo);
		}
		else {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			String downloadKey = PropertyInfoResponseHandler.DOWNLOAD_KEY_PREFIX + mProperty.getPropertyId();
			if (bd.isDownloading(downloadKey)) {
				bd.registerDownloadCallback(downloadKey, this);
			}
			else {
				bd.startDownload(downloadKey, this, this);
				TrackingUtils.trackSimpleEvent(mActivity.getApplicationContext(), null, null, "Shopper",
						"App.Hotels.BD.ExpandRoomDetails");
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Download methods

	@Override
	public Object doDownload() {
		ExpediaServices services = new ExpediaServices(mActivity.getApplicationContext());
		return services.info(mProperty);
	}

	@Override
	public void onDownload(Object results) {
		onPropertyInfoDownloaded((PropertyInfoResponse) results);
	}

}
