package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch.FlightTripQuery;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IResultsFlightSelectedListener;
import com.expedia.bookings.widget.FlightAdapter;
import com.expedia.bookings.widget.SimpleColorAdapter;
import com.expedia.bookings.widget.TabletFlightAdapter;
import com.mobiata.android.util.Ui;

/**
 * ResultsFlightListFragment: The flight list fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightListFragment extends ResultsListFragment {

	private static final String STATE_LEG_NUMBER = "STATE_LEG_NUMBER";

	private ListAdapter mAdapter;
	private int mLegNumber = -1;
	private IResultsFlightSelectedListener mFlightSelectedListener;

	public static ResultsFlightListFragment getInstance(int legPosition) {
		ResultsFlightListFragment frag = new ResultsFlightListFragment();
		frag.setLegPosition(legPosition);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mLegNumber = savedInstanceState.getInt(STATE_LEG_NUMBER, -1);
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_LEG_NUMBER, mLegNumber);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mFlightSelectedListener = Ui.findFragmentListener(this, IResultsFlightSelectedListener.class);
	}

	public void setLegPosition(int legNumber) {
		mLegNumber = legNumber;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mFlightSelectedListener.onFlightSelected(mLegNumber);
	}

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

			// Make sure mLegNumber is in-bounds (should not be needed in production)
			mLegNumber = Math.min(mLegNumber, Db.getFlightSearch().getSelectedLegs().length - 1);

			// Setup data
			adapter.setLegPosition(mLegNumber);

			if (mLegNumber > 0) {
				FlightTripQuery previousQuery = Db.getFlightSearch().queryTrips(mLegNumber - 1);
				adapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegNumber),
						previousQuery.getMinTime(),
						previousQuery.getMaxTime());
			}
			else {
				adapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegNumber));
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
		return new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ResultsFlightListFragment.this.gotoBottomPosition();
			}

		};
	}

	@Override
	protected boolean initializeSortAndFilterEnabled() {
		return mLegNumber <= 0;
	}
}
