package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.LayoutUtils;
import com.mobiata.android.widget.TiltedImageView;

public class RoomTypeWidget {

	private static final String INSTANCE_EXPANDED = "RoomTypeWidget.INSTANCE_EXPANDED";

	private Context mContext;

	private View mRoomTypeRow;
	private TextView mRoomTypeTextView;
	private TiltedImageView mDisplayArrow;
	private TextView mRoomDetailsTextView;

	private boolean mIsExpandable;
	private OnClickListener mOnRowClickListener;

	public RoomTypeWidget(Context context, boolean isExpandable) {
		mContext = context;
		mIsExpandable = isExpandable;

		// Inflate the view
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View roomTypeRow = mRoomTypeRow = inflater.inflate(R.layout.snippet_room_type_widget, null);

		// Cache the views
		mRoomTypeTextView = (TextView) roomTypeRow.findViewById(R.id.room_type_text_view);
		mDisplayArrow = (TiltedImageView) roomTypeRow.findViewById(R.id.display_arrow);
		mRoomDetailsTextView = (TextView) roomTypeRow.findViewById(R.id.room_details_text_view);

		// Configure expansion (or not) of room type
		if (isExpandable) {
			mOnRowClickListener = new OnClickListener() {
				public void onClick(View v) {
					toggleDetails();
				}
			};

			addClickableView(roomTypeRow);
		}
		else {
			mDisplayArrow.setVisibility(View.GONE);
		}
	}

	// Returns the backing View, allowing people to add/remove it as desired
	public View getView() {
		return mRoomTypeRow;
	}

	// We may want to add clickable rows outside of the handler, to expand the tap area; this allows
	// you to do that.  Warning, destroys existing OnClickListener (if were any on the View).
	public void addClickableView(View view) {
		if (mIsExpandable && view != null) {
			view.setOnClickListener(mOnRowClickListener);
		}
	}

	public void toggleDetails() {
		setDetailsState(mRoomDetailsTextView.getVisibility() == View.GONE);
	}

	private void setDetailsState(boolean expanded) {
		if (expanded) {
			mDisplayArrow.setRotation(180);
			mRoomDetailsTextView.setVisibility(View.VISIBLE);
		}
		else {
			mDisplayArrow.setRotation(0);
			mRoomDetailsTextView.setVisibility(View.GONE);
		}
	}

	public void updateRate(Rate rate) {
		mRoomTypeTextView.setText(Html.fromHtml(rate.getRoomDescription()));
		LayoutUtils.layoutRoomLongDescription(mContext, rate, mRoomDetailsTextView);
	}

	public void saveInstanceState(Bundle outState) {
		outState.putBoolean(INSTANCE_EXPANDED, mRoomDetailsTextView.getVisibility() == View.VISIBLE);
	}

	public void restoreInstanceState(Bundle savedState) {
		if (savedState != null) {
			if (savedState.getBoolean(INSTANCE_EXPANDED, false)) {
				setDetailsState(true);
			}
		}
	}
}
