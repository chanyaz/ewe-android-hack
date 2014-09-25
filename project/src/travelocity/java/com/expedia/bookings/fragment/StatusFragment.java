package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.AbsStatusFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LayoutUtils;
import com.mobiata.android.util.Ui;

public class StatusFragment extends AbsStatusFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		View flightSearchView = Ui.findView(v, R.id.search_progress_flight_tvly);
		flightSearchView.bringToFront();
		mMessageTextView.bringToFront();
		displayStatus();

		return v;
	}

}
