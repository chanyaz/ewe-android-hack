package com.expedia.bookings.fragment;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class FlightListFragment extends ListFragment {

	private FlightListFragmentListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof FlightListFragmentListener)) {
			throw new RuntimeException("FlightListFragment Activity must implement FlightListFragmentListener!");
		}

		mListener = (FlightListFragmentListener) activity;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mListener.onFlightClick(position);
	}

	public interface FlightListFragmentListener {

		public void onFlightClick(int position);
	}
}
