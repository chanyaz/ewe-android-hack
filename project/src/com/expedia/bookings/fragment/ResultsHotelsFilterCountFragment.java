package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelFilter.OnFilterChangedListener;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.widget.RingedCountView;
import com.mobiata.android.util.Ui;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
public class ResultsHotelsFilterCountFragment extends Fragment {

	private RingedCountView mRingView;

	public static ResultsHotelsFilterCountFragment newInstance() {
		ResultsHotelsFilterCountFragment frag = new ResultsHotelsFilterCountFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_filtered_count, null);

		mRingView = Ui.findView(view, R.id.ring);
		mRingView.setCount(0);
		mRingView.setPercent(0f);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		update();
	}

	@Override
	public void onStart() {
		super.onStart();

		HotelFilter filter = Db.getFilter();
		if (filter != null) {
			filter.addOnFilterChangedListener(mListener);
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		HotelFilter filter = Db.getFilter();
		if (filter != null) {
			filter.removeOnFilterChangedListener(mListener);
		}
	}

	OnFilterChangedListener mListener = new OnFilterChangedListener() {
		public void onFilterChanged() {
			update();
		}
	};

	public void update() {
		int count = 0;
		int total = 0;

		HotelSearchResponse response = Db.getHotelSearch().getSearchResponse();
		if (response != null) {
			total = response.getPropertiesCount();
			count = response.getFilteredAndSortedProperties().length;
		}

		float percent = total == 0 ? 0 : 1.0f * count / total;

		mRingView.setCaption(String.format("of %d hotels", total));
		mRingView.animateTo(count, percent);
	}
}
