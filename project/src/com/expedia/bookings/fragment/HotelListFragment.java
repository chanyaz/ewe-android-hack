package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.activity.TabletActivity.EventHandler;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.widget.HotelAdapter;

public class HotelListFragment extends ListFragment implements EventHandler {

	private HotelAdapter mAdapter;

	private TextView mMessageTextView;

	public static HotelListFragment newInstance() {
		return new HotelListFragment();
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		((TabletActivity) activity).registerEventHandler(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new HotelAdapter(getActivity());
		setListAdapter(mAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_list, container, false);

		mMessageTextView = (TextView) view.findViewById(android.R.id.empty);

		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_SEARCH_STARTED:
			mAdapter.setSearchResponse(null);
			mMessageTextView.setText(R.string.progress_searching_hotels);
			break;
		case TabletActivity.EVENT_SEARCH_PROGRESS:
			mMessageTextView.setText((String) data);
			break;
		case TabletActivity.EVENT_SEARCH_COMPLETE:
			mAdapter.setSearchResponse((SearchResponse) data);
			break;
		case TabletActivity.EVENT_SEARCH_ERROR:
			mMessageTextView.setText((String) data);
			break;
		}
	}
}
