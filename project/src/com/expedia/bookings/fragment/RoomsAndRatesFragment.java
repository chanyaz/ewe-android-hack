package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.widget.RoomsAndRatesAdapter;

public class RoomsAndRatesFragment extends ListFragment implements EventHandler {

	public static RoomsAndRatesFragment newInstance() {
		RoomsAndRatesFragment fragment = new RoomsAndRatesFragment();
		return fragment;
	}

	private RoomsAndRatesAdapter mAdapter;
	private TextView mMessageTextView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((TabletActivity) activity).registerEventHandler(this);
	}

	@Override
	public void onDetach() {
		((TabletActivity) getActivity()).unregisterEventHandler(this);
		super.onDetach();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		AvailabilityResponse response = ((TabletActivity) getActivity()).getRoomsAndRatesAvailability();
		if (response != null) {
			mAdapter = new RoomsAndRatesAdapter(getActivity(), response);
			setListAdapter(mAdapter);
		} else {
			mMessageTextView.setText(getString(R.string.room_rates_loading));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_availability_list, container, false);
		mMessageTextView = (TextView) view.findViewById(android.R.id.empty);
		return view;

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		((TabletActivity) getActivity()).rateSelected((Rate) mAdapter.getItem(position));
	}

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_AVAILABILITY_SEARCH_COMPLETE:
			mAdapter = new RoomsAndRatesAdapter(getActivity(), (AvailabilityResponse) data);
			setListAdapter(mAdapter);
			break;
		case TabletActivity.EVENT_AVAILABILITY_SEARCH_ERROR:
			mMessageTextView.setText((String) data);
			break;
		case TabletActivity.EVENT_AVAILABILITY_SEARCH_STARTED:
			mMessageTextView.setText((String) data);
			break;
		}
	}
}
