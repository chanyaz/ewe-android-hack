package com.expedia.bookings.fragment;

import android.app.Fragment;

import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.Session;

/**
 * This Fragment is designed for retaining data across orientation changes or what have you.
 * 
 * It is invisible and not on the back stack.
 */
public class InstanceFragment extends Fragment {

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
	public Rate mRate;
	public String mPropertyInfoStatus;
	public PropertyInfoResponse mPropertyInfoResponse;
	public AvailabilityResponse mAvailabilityResponse;
	public ReviewsResponse mReviewsResponse;
}
