package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Build;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;

import com.expedia.bookings.R;
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

	private ListAdapter mAdapter;

	@Override
	protected ListAdapter initializeAdapter() {
		if (Db.getFlightSearch() == null || Db.getFlightSearch().getSearchResponse() == null) {
			int[] flightColors = { Color.rgb(0, 0, 255), Color.rgb(0, 0, 220), Color.rgb(0, 0, 150) };
			mAdapter = new SimpleColorAdapter(getActivity(), 200, 50, flightColors);
			//mAdapter.enableSizeChanges(10, 3000);
		}

		else {
			FlightAdapter adapter = new TabletFlightAdapter(getActivity(), null);
			mAdapter = adapter;
			mAdapter.registerDataSetObserver(new DataSetObserver() {
				public void onChanged() {
					initializeStickyHeaderString();
				}
			});

			// Setup data
			int mLegPosition = 0;
			adapter.setLegPosition(mLegPosition);

			if (mLegPosition > 0) {
				FlightTripQuery previousQuery = Db.getFlightSearch().queryTrips(mLegPosition - 1);
				adapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegPosition),
						previousQuery.getMinTime(),
						previousQuery.getMaxTime());
			}
			else {
				adapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegPosition));
			}
		}
		return mAdapter;
	}

	@Override
	protected CharSequence initializeStickyHeaderString() {
		int count = mAdapter == null ? 0 : mAdapter.getCount();
		CharSequence text = getResources().getQuantityString(R.plurals.number_of_flights_TEMPLATE,
				count, count);
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
