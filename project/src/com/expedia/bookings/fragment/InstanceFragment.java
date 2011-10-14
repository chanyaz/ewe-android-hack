package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.Session;
import com.expedia.bookings.fragment.EventManager.EventHandler;

/**
 * This Fragment is designed for retaining data across orientation changes or what have you.
 * 
 * It is invisible and not on the back stack.
 */
public class InstanceFragment extends Fragment implements EventHandler {

	public static InstanceFragment newInstance() {
		InstanceFragment instance = new InstanceFragment();

		// Configure the InstanceFragment to retain on rotation
		instance.setRetainInstance(true);

		// Initialize the member variables
		instance.mSearchParams = new SearchParams();
		instance.mFilter = new Filter();

		return instance;
	}

	//////////////////////////////////////////////////////////////////////////
	// Retained data

	public SearchParams mSearchParams;
	public Session mSession;
	public String mSearchStatus;
	public SearchResponse mSearchResponse;
	public Filter mFilter;
	public Property mProperty;
	public AvailabilityResponse mAvailabilityResponse;
	public ReviewsResponse mReviewsResponse;

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_SEARCH_STARTED:
			mSearchStatus = getString(R.string.progress_searching_hotels);
			break;
		case TabletActivity.EVENT_SEARCH_PROGRESS:
		case TabletActivity.EVENT_SEARCH_ERROR:
			mSearchStatus = (String) data;
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((TabletActivity) getActivity()).registerEventHandler(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}
}
