package com.expedia.bookings.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.mobiata.flightlib.data.RecentSearchList;
import com.mobiata.flightlib.widget.AirportAdapter;

public class AirportPickerFragment extends ListFragment {
	public static final String TAG = AirportPickerFragment.class.getCanonicalName();

	private static final String RECENT_SEARCH_LIST_FILE = "recent-airports.dat";

	private static final String STATE_FILTER = "STATE_FILTER";

	private AirportAdapter mAdapter;

	private AirportPickerFragmentListener mListener;

	private RecentSearchList mRecentSearchList;

	// Stores what should be the initially filtered text when
	// the view is created.  Held due to timing issues that
	// crop up without its assistance.
	private String mInitialFilter = null;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof AirportPickerFragmentListener)) {
			throw new RuntimeException(
					"AirportPickerFragment activity must implement AirportPickerFragmentListener!");
		}

		mListener = (AirportPickerFragmentListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mInitialFilter = savedInstanceState.getString(STATE_FILTER);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getListView().setBackgroundColor(Color.WHITE);

		mAdapter = new AirportAdapter(getActivity());
		mAdapter.openDb();

		mRecentSearchList = new RecentSearchList(getActivity(), RECENT_SEARCH_LIST_FILE);
		mAdapter.mRecentSearchList = mRecentSearchList;

		mAdapter.filter(mInitialFilter != null ? mInitialFilter : "");

		setListAdapter(mAdapter);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(STATE_FILTER, mInitialFilter);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		if (mAdapter != null) {
			mAdapter.closeDb();
			mAdapter = null;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (id == AirportAdapter.ID_CLEAR_RECENT) {
			// Clear recents
			mRecentSearchList.clear();
			mRecentSearchList.saveList(getActivity(), RECENT_SEARCH_LIST_FILE);

			// Update the adapter
			mAdapter.filter("");
			mAdapter.notifyDataSetChanged();
		}
		else {
			String airportCode = mAdapter.getAirportCode(position);

			// Save airport to recent search list
			mRecentSearchList.addItem(airportCode);
			mRecentSearchList.saveList(getActivity(), RECENT_SEARCH_LIST_FILE);

			mListener.onAirportClick(airportCode);
		}
	}

	public void filter(CharSequence constraint) {
		mInitialFilter = constraint.toString();
		if (mAdapter != null) {
			mAdapter.filter(constraint);
		}
	}

	public interface AirportPickerFragmentListener {
		public void onAirportClick(String airportCode);
	}
}