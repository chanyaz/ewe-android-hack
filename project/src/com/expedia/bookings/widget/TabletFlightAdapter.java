package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;

/**
 * Most of the functionality extends from FlightAdapter. This class
 * just contains handling that is specific to tablet.
 */
public class TabletFlightAdapter extends FlightAdapter {

	LayoutInflater mInflater;

	public TabletFlightAdapter(Context context, Bundle savedInstanceState) {
		super(context, savedInstanceState);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.section_flight_leg_tablet_blue_card, parent, false);
		}

		return super.getView(position, convertView, parent);
	}
}
