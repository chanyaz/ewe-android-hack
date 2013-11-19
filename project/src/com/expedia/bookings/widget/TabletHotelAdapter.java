package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;

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

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.section_hotel_summary_tablet, parent, false);
		}

		return super.getView(position, convertView, parent);
	}
}
