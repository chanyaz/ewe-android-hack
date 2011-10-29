package com.expedia.bookings.widget;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.PropertyInfo;
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.utils.BookingReceiptUtils;
import com.mobiata.android.Log;
import com.mobiata.android.widget.TiltedImageView;

/**
 * Adds a room type row anywhere, and sets up its behavior.
 * 
 * Note that in order for this to work properly, you must call the lifecycle methods
 * (onPause(), onResume(), etc) at the proper times.
 */
public abstract class RoomTypeHandler {

	private Context mContext;
	private SearchParams mSearchParams;

	protected Property mProperty;
	protected Rate mRate;
	protected PropertyInfo mPropertyInfo;

	protected View mRoomTypeRow;
	protected View mRoomTypeRowContainer;

	private TiltedImageView mDisplayArrow;
	private ViewGroup mRoomDetailsLayout;
	private ProgressBar mProgressBar;
	private TextView mRoomDetailsTextView;

	private OnClickListener mOnRowClickListener;

	//////////////////////////////////////////////////////////////////////////
	// Public methods
	
	public RoomTypeHandler(Context context, Intent intent, Property property, SearchParams searchParams, Rate rate) {
		mContext = context;
		mProperty = property;
		mSearchParams = searchParams;
		mRate = rate;

		String propertyInfoString = (intent != null) ? intent.getStringExtra(Codes.PROPERTY_INFO) : null;
		if (propertyInfoString != null) {
			try {
				PropertyInfo propertyInfo = new PropertyInfo();
				propertyInfo.fromJson(new JSONObject(propertyInfoString));
				mPropertyInfo = propertyInfo;
			}
			catch (JSONException e) {
				Log.i("Unable to get property info object from the previous state", e);
			}
		}

		// Inflate the view
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View roomTypeRow = mRoomTypeRow = inflater.inflate(R.layout.snippet_room_type, null);

		// Cache the views
		mDisplayArrow = (TiltedImageView) roomTypeRow.findViewById(R.id.display_arrow);
		mRoomDetailsLayout = (ViewGroup) roomTypeRow.findViewById(R.id.room_details_layout);
		mProgressBar = (ProgressBar) roomTypeRow.findViewById(R.id.progress_bar);
		mRoomDetailsTextView = (TextView) roomTypeRow.findViewById(R.id.room_details_text_view);

		// Initial configuration of the views
		updateRoomTypeDescription();
		
		// Configure behavior
		ViewGroup roomTypeLayout = (ViewGroup) roomTypeRow.findViewById(R.id.room_type_layout);
		mOnRowClickListener = new OnClickListener() {
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
		};
		addClickableView(roomTypeLayout);
		addClickableView(mRoomDetailsLayout);
	}
	
	// We may want to add clickable rows outside of the handler, to expand the tap area; this allows
	// you to do that.  Warning, destroys existing OnClickListener (if were any on the View).
	public void addClickableView(View view) {
		view.setOnClickListener(mOnRowClickListener);
	}

	public void load(ViewGroup container) {
		container.addView(mRoomTypeRow);
		mRoomTypeRowContainer = container;
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
	// Protected methods

	protected void updateRoomTypeDescription() {
		TextView valueView = (TextView) mRoomTypeRow.findViewById(R.id.value_text_view);
		valueView.setText(Html.fromHtml(mRate.getRoomDescription()));
	}

	protected void showDetails(String details) {
		mRoomDetailsTextView.setText(Html.fromHtml(details));
		mRoomDetailsTextView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
	}

	protected void showCheckInCheckoutDetails(PropertyInfo propertyInfo) {
		String start = BookingReceiptUtils.formatCheckInOutDate(mContext, mSearchParams.getCheckInDate());
		String end = BookingReceiptUtils.formatCheckInOutDate(mContext, mSearchParams.getCheckOutDate());
		TextView checkInTimeTextView = (TextView) mRoomTypeRowContainer.findViewById(R.id.check_in_time);
		TextView checkOutTimeTextView = (TextView) mRoomTypeRowContainer.findViewById(R.id.check_out_time);

		if (propertyInfo == null) {
			checkInTimeTextView.setText(start);
			checkOutTimeTextView.setText(end);
		}
		else {
			String checkInText = (propertyInfo.getCheckInTime() == null) ? start : mContext.getString(
					R.string.check_in_out_time_template, propertyInfo.getCheckInTime(), start);
			String checkOutText = (propertyInfo.getCheckOutTime() == null) ? end : mContext.getString(
					R.string.check_in_out_time_template, propertyInfo.getCheckOutTime(), end);
			checkInTimeTextView.setText(checkInText);
			checkOutTimeTextView.setText(checkOutText);
		}
	}

	protected void showDetails(PropertyInfo propertyInfo) {
		String details = propertyInfo.getRoomLongDescription(mRate);
		if (details == null || details.length() == 0) {
			showDetails(mContext.getString(R.string.error_room_type_nonexistant));
		}
		else {
			showDetails(details);
		}
	}

	protected void onPropertyInfoDownloaded(PropertyInfoResponse response) {
		if (response == null) {
			showDetails(mContext.getString(R.string.error_room_type_load));
			showCheckInCheckoutDetails(null);
		}
		else if (response.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			for (ServerError error : response.getErrors()) {
				sb.append(error.getPresentableMessage(mContext));
				sb.append("\n");
			}
			showDetails(sb.toString().trim());
			showCheckInCheckoutDetails(null);
		}
		else {
			mPropertyInfo = response.getPropertyInfo();
			if (isExpanded()) {
				showDetails(mPropertyInfo);
			}
			showCheckInCheckoutDetails(mPropertyInfo);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle Methods

	public abstract void onDestroy();

	public abstract void onResume();

	public abstract void onCreate(Bundle savedInstanceState);

	public abstract void loadDetails();
}
