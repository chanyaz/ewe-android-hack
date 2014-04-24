package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.section.HotelSummarySection;
import com.mobiata.android.Log;

/**
 * Most of the functionality extends from HotelAdapter. This class
 * just contains handling that is specific to tablet.
 */
public class TabletHotelAdapter extends HotelAdapter {

	LayoutInflater mInflater;
	Context mContext;

	public TabletHotelAdapter(Activity activity) {
		super(activity);
		mInflater = LayoutInflater.from(activity);
		mContext = activity;
	}

	/**
	 * Will return true if the row at position [position] is expandable. In this case, it means
	 * if there are Mobile Exclusive Deals or other Urgency Messages for this hotel.
	 * @param position
	 * @return
	 */
	@Override
	public boolean isRowExpandable(int position) {
		// The logic here should basically mirror what's in HotelSummarySection
		if (ExpediaBookingApp.IS_VSC) {
			return false;
		}

		Property property = (Property) getItem(position);
		if (property.isLowestRateTonightOnly()) {
			return true;
		}

		if (property.isLowestRateMobileExclusive()) {
			return true;
		}

		int roomsLeft = property.getRoomsLeftAtThisRate();
		if (roomsLeft > 0 && roomsLeft <= HotelSummarySection.ROOMS_LEFT_CUTOFF) {
			return true;
		}

		return false;
	}

	/**
	 * Returns the estimated height for the view at position [position].
	 * @param position
	 * @return
	 */
	public int estimateViewHeight(int position) {
		Resources res = mContext.getResources();
		int height = res.getDimensionPixelSize(R.dimen.tablet_tripbucket_card_height);
		if (isRowExpandable(position)) {
			height += res.getDimensionPixelSize(R.dimen.hotel_row_med_container_height);
		}
		return height;
	}

	/**
	 * returns the number of pixels by which the row at position [position] expands
	 * (if it's expandable at all).
	 * @param position
	 * @return
	 */
	public int estimateExpandableHeight(int position) {
		return isRowExpandable(position)
			? mContext.getResources().getDimensionPixelSize(R.dimen.hotel_row_med_container_height)
			: 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.section_hotel_summary_tablet, parent, false);
		}

		return super.getView(position, convertView, parent);
	}
}
