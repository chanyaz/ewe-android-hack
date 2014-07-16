package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.WeeklyFlightHistogramAdapter;

/**
 * ResultsFlightHistogramFragment: The flight histogram fragment designed for tablet results 2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightHistogramFragment extends ListFragment {

	public interface IFlightHistogramListener {
		public void onGdeDateSelected(LocalDate date);
	}

	private ListView mList;
	private WeeklyFlightHistogramAdapter mAdapter;
	private IFlightHistogramListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, IFlightHistogramListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mList = Ui.inflate(inflater, R.layout.fragment_tablet_results_flight_histogram, container, false);

		// Adapter setup
		if (mAdapter == null) {
			mAdapter = new WeeklyFlightHistogramAdapter(getActivity());
		}
		if (Db.getFlightSearchHistogramResponse() != null) {
			mAdapter.setHistogramData(Db.getFlightSearchHistogramResponse());
		}
		mList.setAdapter(mAdapter);

		return mList;
	}

	public void setHistogramData(FlightSearchHistogramResponse data) {
		if (mAdapter != null) {
			mAdapter.setHistogramData(data);
		}
	}

	public void setSelectedDepartureDate(LocalDate departureDate) {
		if (mAdapter != null) {
			mAdapter.setSelectedDepartureDate(departureDate);
		}
	}

	@Override
	public void onListItemClick(ListView list, View view, int pos, long id) {
		if (list == mList && list.getAdapter() != null && list.getAdapter() == mAdapter) {
//			FlightHistogram histo = mAdapter.getItem(pos);
//			if (histo != null) {
//				mListener.onGdeDateSelected(histo.getKeyDate());
//			}
			//TODO: item click
		}
	}

}
