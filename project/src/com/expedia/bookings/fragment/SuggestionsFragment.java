package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.widget.AirportDropDownAdapter;
import com.mobiata.android.util.Ui;

public class SuggestionsFragment extends ListFragment {

	private SuggestionsFragmentListener mListener;

	private AirportDropDownAdapter mAirportAdapter;

	// Sometimes we want to prep text to filter before start; by default
	// we start with a blank query (to kick off the defaults)
	private CharSequence mTextToFilterOnCreate = "";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, SuggestionsFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_suggestions, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (mAirportAdapter == null) {
			mAirportAdapter = new AirportDropDownAdapter(getActivity());
			mAirportAdapter.setShowNearbyAirports(true);
		}

		setListAdapter(mAirportAdapter);

		filter(mTextToFilterOnCreate);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Location location = mAirportAdapter.getLocation(position);
		mAirportAdapter.onAirportSelected(location);
		mListener.onSuggestionClicked(this, location);
	}

	public void filter(CharSequence text) {
		if (text == null) {
			text = "";
		}

		if (getView() != null) {
			mAirportAdapter.getFilter().filter(text);
		}
		else {
			mTextToFilterOnCreate = text;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface SuggestionsFragmentListener {
		public void onSuggestionClicked(Fragment fragment, Location location);
	}
}
