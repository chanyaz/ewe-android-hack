package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelFilter.OnFilterChangedListener;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.widget.RingedCountView;
import com.mobiata.android.util.Ui;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
public class ResultsHotelsFilterCountFragment extends Fragment implements OnFilterChangedListener {

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

		Db.getFilter().addOnFilterChangedListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		Db.getFilter().removeOnFilterChangedListener(this);
	}

	@Override
	public void onFilterChanged() {
		update();
	}

	public void update() {
		int count = 0;
		int total = 0;

		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		HotelSearchResponse response = Db.getHotelSearch().getSearchResponse();
		if (response != null) {
			total = response.getPropertiesCount();
			count = response.getFilteredPropertiesCount(params);
		}

		float percent = total == 0 ? 0 : 1.0f * count / total;

		String caption = getResources().getQuantityString(R.plurals.of_y_hotels_TEMPLATE, total, total);

		mRingView.setCaption(caption);
		mRingView.animateTo(count, percent);
	}
}
