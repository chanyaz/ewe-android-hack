package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.AbsFlightSearchLoadingFragment;
import com.expedia.bookings.utils.Ui;

/**
 * Not needed
 */
public class FlightSearchLoadingFragment extends AbsFlightSearchLoadingFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		View flightSearchView = Ui.findView(v, R.id.search_progress_flight_vsc);
		flightSearchView.bringToFront();
		mMessageTextView.bringToFront();
		mMessageTextView.setTextColor(getResources().getColor(R.color.flight_list_progress_text_color));
		displayStatus();

		return v;
	}
}
