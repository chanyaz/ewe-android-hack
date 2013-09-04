package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.widget.SimpleColorAdapter;

import android.annotation.TargetApi;
import android.database.DataSetObserver;
import android.os.Build;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch.FlightTripQuery;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.widget.FlightAdapter;
import com.expedia.bookings.widget.SimpleColorAdapter;
import com.expedia.bookings.widget.TabletFlightAdapter;

/**
 * ResultsFlightListFragment: The flight list fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightListFragment extends ResultsListFragment {

	private FlightAdapter mFlightListAdapter;

	@Override
	protected ListAdapter initializeAdapter() {
<<<<<<< HEAD
		int[] flightColors = { Color.rgb(0, 0, 255), Color.rgb(0, 0, 220), Color.rgb(0, 0, 150) };
		mFlightListAdapter = new SimpleColorAdapter(getActivity(), 200, 50, flightColors);
		//mFlightListAdapter.enableSizeChanges(10, 3000);
=======
		mFlightListAdapter = new TabletFlightAdapter(getActivity(), null);
		mFlightListAdapter.registerDataSetObserver(new DataSetObserver() {
			public void onChanged() {
				initializeStickyHeaderString();
			}
		});

		// Setup data
		int mLegPosition = 0;
		mFlightListAdapter.setLegPosition(mLegPosition);

		if (mLegPosition > 0) {
			FlightTripQuery previousQuery = Db.getFlightSearch().queryTrips(mLegPosition - 1);
			mFlightListAdapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegPosition), previousQuery.getMinTime(),
					previousQuery.getMaxTime());
		}
		else {
			mFlightListAdapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegPosition));
		}

		return mFlightListAdapter;
	}

	@Override
	protected CharSequence initializeStickyHeaderString() {
		int count = mFlightListAdapter == null ? 0 : mFlightListAdapter.getCount();
		CharSequence text = getResources().getQuantityString(R.plurals.number_of_flights_TEMPLATE,
				count, mFlightListAdapter.getCount());
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
