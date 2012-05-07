package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.mobiata.flightlib.widget.AirportAdapter;

public class AirlinePickerFragment extends ListFragment {
	public static final String TAG = AirlinePickerFragment.class.getCanonicalName();

	private AirportAdapter mAdapter;

	private AirlinePickerFragmentListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof AirlinePickerFragmentListener)) {
			throw new RuntimeException(
					"AirlinePickerFragment activity must implement AirlinePickerFragmentListener!");
		}

		mListener = (AirlinePickerFragmentListener) activity;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mAdapter = new AirportAdapter(getActivity());
		mAdapter.openDb();
		mAdapter.filter("");

		setListAdapter(mAdapter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mAdapter.closeDb();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		mListener.onAirportClick(mAdapter.getAirportCode(position));
	}

	public void filter(CharSequence constraint) {
		mAdapter.filter(constraint);
	}

	public interface AirlinePickerFragmentListener {
		public void onAirportClick(String airportCode);
	}
}