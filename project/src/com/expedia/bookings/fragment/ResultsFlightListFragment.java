package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.widget.SimpleColorAdapter;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;

/**
 * ResultsFlightListFragment: The flight list fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightListFragment extends ResultsListFragment {

	private SimpleColorAdapter mFlightListAdapter;

	@Override
	protected ListAdapter initializeAdapter() {
		int[] flightColors = { Color.rgb(0, 0, 255), Color.rgb(0, 0, 220), Color.rgb(0, 0, 150) };
		mFlightListAdapter = new SimpleColorAdapter(getActivity(), 200, 50, flightColors);
		//mFlightListAdapter.enableSizeChanges(10, 3000);
		return mFlightListAdapter;
	}

	@Override
	protected CharSequence initializeStickyHeaderString() {
		CharSequence text = getResources().getQuantityString(R.plurals.number_of_flights_TEMPLATE,
				mFlightListAdapter.getCount(), mFlightListAdapter.getCount());
		return text;
	}

	@Override
	protected OnClickListener initializeSortAndFilterOnClickListener() {
		return null;
	}

	@Override
	protected boolean initializeSortAndFilterEnabled() {
		return false;
	}
}
