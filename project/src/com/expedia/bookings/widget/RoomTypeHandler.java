package com.expedia.bookings.widget;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.tracking.TrackingUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.widget.TiltedImageView;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.PropertyInfo;
import com.mobiata.hotellib.data.PropertyInfoResponse;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.data.ServerError;
import com.mobiata.hotellib.server.ExpediaServices;
import com.mobiata.hotellib.server.PropertyInfoResponseHandler;

/**
 * Adds a room type row anywhere, and sets up its behavior.
 * 
 * Note that in order for this to work properly, you must call the lifecycle methods
 * (onPause(), onResume(), etc) at the proper times.
 */
public class RoomTypeHandler implements Download, OnDownloadComplete {

	private Activity mActivity;
	private Property mProperty;
	private Rate mRate;
	private PropertyInfo mPropertyInfo;

	private View mRoomTypeRow;
	private View mRoomTypeRowContainer;
	private TiltedImageView mDisplayArrow;
	private ViewGroup mRoomDetailsLayout;
	private ProgressBar mProgressBar;
	private TextView mRoomDetailsTextView;

	public RoomTypeHandler(Activity activity, Intent intent, Property property, Rate rate) {
		mActivity = activity;
		mProperty = property;
		mRate = rate;
		
		String propertyInfoString = (intent != null) ? intent.getStringExtra(Codes.PROPERTY_INFO) : null;
		if(propertyInfoString != null) {
			try {
				PropertyInfo propertyInfo = new PropertyInfo();
				propertyInfo.fromJson(new JSONObject(propertyInfoString));
				mPropertyInfo = propertyInfo;
			} catch(JSONException e) {
				Log.i("Unable to get property info object from the previous state", e);
			}
		}

		// Inflate the view
		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View roomTypeRow = mRoomTypeRow = inflater.inflate(R.layout.snippet_room_type, null);

		// Cache the views
		mDisplayArrow = (TiltedImageView) roomTypeRow.findViewById(R.id.display_arrow);
		mRoomDetailsLayout = (ViewGroup) roomTypeRow.findViewById(R.id.room_details_layout);
		mProgressBar = (ProgressBar) roomTypeRow.findViewById(R.id.progress_bar);
		mRoomDetailsTextView = (TextView) roomTypeRow.findViewById(R.id.room_details_text_view);

		// Initial configuration of the views
		TextView valueView = (TextView) roomTypeRow.findViewById(R.id.value_text_view);
		valueView.setText(rate.getRoomDescription());

		// Configure behavior
		ViewGroup roomTypeLayout = (ViewGroup) roomTypeRow.findViewById(R.id.room_type_layout);
		roomTypeLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (isExpanded()) {
					setVisibility(View.GONE);
					onDestroy();
				}
				else {
					setVisibility(View.VISIBLE);
					onResume();
				}
			}
		});
	}

	public void load(ViewGroup container) {
		container.addView(mRoomTypeRow);
		mRoomTypeRowContainer = container;
	}

	private void loadDetails() {
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
				TrackingUtils.trackSimpleEvent(mActivity, null, null, "Shopper", "App.Hotels.BD.ExpandRoomDetails");
			}
		}
	}

	private void showDetails(String details) {
		mRoomDetailsTextView.setText(details);
		mRoomDetailsTextView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
	}
	
	private void showCheckInCheckoutDetails(PropertyInfo propertyInfo) {
		TextView checkInTimeTextView = (TextView) mRoomTypeRowContainer.findViewById(1);
		checkInTimeTextView.setText(propertyInfo.getCheckInTime());
		TextView checkOutTimeTextView = (TextView) mRoomTypeRowContainer.findViewById(2);
		checkOutTimeTextView.setText(propertyInfo.getCheckOutTime());
	}

	private void showDetails(PropertyInfo propertyInfo) {
		String details = propertyInfo.getRoomLongDescription(mRate);
		if (details == null || details.length() == 0) {
			showDetails(mActivity.getString(R.string.error_room_type_nonexistant));
		}
		else {
			showDetails(details);
		}
	}

	public void setVisibility(int visibility) {
		if (visibility == View.VISIBLE) {
			mDisplayArrow.setRotation(0);
		}
		else {
			mDisplayArrow.setRotation(90);
		}

		mRoomDetailsLayout.setVisibility(visibility);
	}

	public boolean isExpanded() {
		return mRoomDetailsLayout.getVisibility() == View.VISIBLE;
	}

	//////////////////////////////////////////////////////////////////////////
	// Download methods

	@Override
	public Object doDownload() {
		ExpediaServices services = new ExpediaServices(mActivity);
		return services.info(mProperty);
	}

	@Override
	public void onDownload(Object results) {
		PropertyInfoResponse response = (PropertyInfoResponse) results;
		if (response == null) {
			showDetails(mActivity.getString(R.string.error_room_type_load));
		}
		else if (response.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			for (ServerError error : response.getErrors()) {
				sb.append(error.getPresentableMessage(mActivity));
				sb.append("\n");
			}
			showDetails(sb.toString().trim());
		}
		else {
			mPropertyInfo = response.getPropertyInfo();
			if(isExpanded()) {
				showDetails(mPropertyInfo);
			}
			showCheckInCheckoutDetails(mPropertyInfo);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle methods

	private static final int INSTANCE_PROPERTY_INFO = 101;
	private static final int INSTANCE_EXPANDED = 102;
	private static final int PROPERTY_ROOM_CONTAINER_ID = 103;
	
	@SuppressWarnings("unchecked")
	public void onCreate() {
		Intent intent = mActivity.getIntent();
		mPropertyInfo = (PropertyInfo) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY_INFO,
				PropertyInfo.class);

		SparseArray<Object> instance = (SparseArray<Object>) mActivity.getLastNonConfigurationInstance();
		if (instance != null) {
			if (mPropertyInfo == null) {
				mPropertyInfo = (PropertyInfo) instance.get(INSTANCE_PROPERTY_INFO);
			}
			
			mRoomTypeRowContainer =  mActivity.findViewById(((Integer) instance.get(PROPERTY_ROOM_CONTAINER_ID)).intValue());
			
			if ((Boolean) instance.get(INSTANCE_EXPANDED)) {
				setVisibility(View.VISIBLE);
			}
		}
	}

	public void onDestroy() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		String downloadKey = PropertyInfoResponseHandler.DOWNLOAD_KEY_PREFIX + mProperty.getPropertyId();
		if (bd.isDownloading(downloadKey)) {
			if (mActivity.isFinishing()) {
				bd.unregisterDownloadCallback(downloadKey, this);
			}
		}
	}

	public void onResume() {
		loadDetails();
	}

	public void onRetainNonConfigurationInstance(SparseArray<Object> instance) {
		if (mPropertyInfo != null) {
			instance.put(INSTANCE_PROPERTY_INFO, mPropertyInfo);
		}
		if(mRoomTypeRowContainer != null) {
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
}
