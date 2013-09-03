package com.expedia.bookings.fragment;

import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.widget.SimpleColorAdapter;
import com.mobiata.android.util.Ui;

import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;

/**
 * ResultsHotelListFragment: The hotel list fragment designed for tablet results 2013
 */
public class ResultsHotelListFragment extends ResultsListFragment {

	private SimpleColorAdapter mHotelListAdapter;

	@Override
	protected ListAdapter initializeAdapter() {
		int[] hotelColors = { Color.rgb(255, 0, 0), Color.rgb(220, 0, 0), Color.rgb(150, 0, 0) };
		mHotelListAdapter = new SimpleColorAdapter(getActivity(), 250, 25, hotelColors);
		return mHotelListAdapter;
	}

	@Override
	protected CharSequence initializeStickyHeaderString() {
		CharSequence text = mHotelListAdapter.getCount() + " Hotels";
		return text;
	}

	@Override
	protected OnClickListener initializeSortAndFilterOnClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Ui.showToast(getActivity(), "I'll sort your filters!");
			}
		};
	}

	@Override
	protected boolean initializeSortAndFilterEnabled() {
		return true;
	}
}
