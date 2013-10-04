package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch.FlightTripQuery;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IResultsFlightSelectedListener;
import com.expedia.bookings.widget.FlightAdapter;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
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

	@Override
	public void onDetach() {
		super.onDetach();
		mAdapter.unregisterDataSetObserver(mDataSetObserver);
	}

	public void setLegPosition(int legNumber) {
		mLegNumber = legNumber;
	}

	/**
	 * Call this any time that you need to reset the query. Basically, any time that you move forward,
	 * in the flights flow, by selecting a leg, you want to clear the query of the next leg. The reason 
	 * is that the flights present in the second leg is dependent upon which flight has been selected
	 * from the first leg. This method call takes care of resetting the query, and ensuring all observers
	 * are setup to properly receive changes, from the filter, for instance.
	 */
	public void resetQuery() {
		Db.getFlightSearch().setSelectedLeg(mLegNumber, null);
		Db.getFlightSearch().clearQuery(mLegNumber);
		resetAdapterQuery();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO remove this if check once SimpleColorAdapter is removed
		if (mAdapter instanceof FlightAdapter) {
			int headerCount = getTopSpaceListView().getHeaderViewsCount();
			int itemPosition = position - headerCount;
			if (itemPosition >= 0) {
				FlightTrip trip = ((FlightAdapter) mAdapter).getItem(itemPosition);
				Db.getFlightSearch().setSelectedLeg(mLegNumber, new FlightTripLeg(trip, trip.getLeg(mLegNumber)));
				mFlightSelectedListener.onFlightSelected(mLegNumber);
			}
		}
	}

	@Override
	protected ListAdapter initializeAdapter() {
		// TODO: this block is temporary
		if (Db.getFlightSearch() == null || Db.getFlightSearch().getSearchResponse() == null) {
			int[] flightColors = { Color.rgb(0, 0, 255), Color.rgb(0, 0, 220), Color.rgb(0, 0, 150) };
			mAdapter = new SimpleColorAdapter(getActivity(), 200, 50, flightColors);
			//mAdapter.enableSizeChanges(10, 3000);
			return mAdapter;
		}

		FlightAdapter adapter = new TabletFlightAdapter(getActivity(), null);
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataSetObserver);

		// Make sure mLegNumber is in-bounds (should not be needed in production)
		mLegNumber = Math.min(mLegNumber, Db.getFlightSearch().getSelectedLegs().length - 1);

		// Setup data
		adapter.setLegPosition(mLegNumber);

		resetAdapterQuery();
		return mAdapter;
	}

	private void resetAdapterQuery() {
		FlightAdapter adapter = (FlightAdapter) mAdapter;
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

	@Override
	protected CharSequence initializeStickyHeaderString() {
		int count = mAdapter == null ? 0 : mAdapter.getCount();
		CharSequence text = getResources().getQuantityString(R.plurals.x_Flights_TEMPLATE, count, count);
		return text;
	}

	@Override
	protected OnClickListener initializeTopRightTextButtonOnClickListener() {
		return new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (getTopSpaceListView() != null && getTopSpaceListView().getState() == State.LIST_CONTENT_AT_TOP) {
					ResultsFlightListFragment.this.gotoBottomPosition();
				}
			}

		};
	}

	@Override
	protected boolean initializeTopRightTextButtonEnabled() {
		return mLegNumber <= 0;
	}

	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			setStickyHeaderText(initializeStickyHeaderString());
		}
	};

}
