package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.DataSetObserver;
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
import com.expedia.bookings.enums.ResultsFlightsListState;
import com.expedia.bookings.enums.ResultsListState;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IResultsFlightSelectedListener;
import com.expedia.bookings.widget.FlightAdapter;
import com.expedia.bookings.widget.TabletFlightAdapter;
import com.mobiata.android.util.Ui;

/**
 * ResultsFlightListFragment: The flight list fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightListFragment extends ResultsListFragment<ResultsFlightsListState> {

	public interface IDoneClickedListener {
		public void onDoneClicked();
	}

	private static final String STATE_LEG_NUMBER = "STATE_LEG_NUMBER";

	private ListAdapter mAdapter;
	private int mLegNumber = -1;
	private IResultsFlightSelectedListener mFlightSelectedListener;
	private IDoneClickedListener mDoneClickedListener;

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
		mDoneClickedListener = Ui.findFragmentListener(this, IDoneClickedListener.class, false);
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
		int headerCount = getListView().getHeaderViewsCount();
		int itemPosition = position - headerCount;
		if (itemPosition >= 0) {
			FlightTrip trip = ((FlightAdapter) mAdapter).getItem(itemPosition);
			if (trip != null) {
				Db.getFlightSearch().setSelectedLeg(mLegNumber, new FlightTripLeg(trip, trip.getLeg(mLegNumber)));
				mFlightSelectedListener.onFlightSelected(mLegNumber);
				mListView.setItemChecked(position, true);
			}
		}
	}

	@Override
	protected ListAdapter initializeAdapter() {
		FlightAdapter adapter = new TabletFlightAdapter();
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataSetObserver);

		// Make sure mLegNumber is in-bounds (should not be needed in production)
		// TODO still needed?
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
				if (mDoneClickedListener == null) {
					ResultsFlightListFragment.this.setPercentage(1f, 200);
				}
				else {
					mDoneClickedListener.onDoneClicked();
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

	@Override
	protected ResultsFlightsListState translateState(ResultsListState state) {
		if (state == ResultsListState.AT_TOP) {
			return ResultsFlightsListState.FLIGHTS_LIST_AT_TOP;
		}
		else if (state == ResultsListState.AT_BOTTOM) {
			return ResultsFlightsListState.FLIGHTS_LIST_AT_BOTTOM;
		}
		return null;
	}

	@Override
	protected ResultsFlightsListState getDefaultState() {
		return ResultsFlightsListState.FLIGHTS_LIST_AT_BOTTOM;
	}

}
