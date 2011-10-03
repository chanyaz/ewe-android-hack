package com.expedia.bookings.fragment;

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

	private TextView mMessageTextView;

	public static HotelListFragment newInstance() {
		return new HotelListFragment();
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_list, container, false);

		mMessageTextView = (TextView) view.findViewById(android.R.id.empty);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		((TabletActivity) getActivity()).registerEventHandler(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_SEARCH_STARTED:
			setListAdapter(null);
			mMessageTextView.setText(R.string.progress_searching_hotels);
			break;
		case TabletActivity.EVENT_SEARCH_PROGRESS:
			mMessageTextView.setText((String) data);
			break;
		case TabletActivity.EVENT_SEARCH_COMPLETE:
			setListAdapter(new HotelAdapter(getActivity(), (SearchResponse) data));
			break;
		case TabletActivity.EVENT_SEARCH_ERROR:
			mMessageTextView.setText((String) data);
			break;
		}
	}
}
