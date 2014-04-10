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
import com.expedia.bookings.data.FlightHistogram;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightHistogramAdapter;

/**
 * ResultsFlightHistogramFragment: The flight histogram fragment designed for tablet results 2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightHistogramFragment extends ListFragment {

	public interface IFlightHistogramListener {
		public void onGdeDateSelected(LocalDate date);
	}

	private ListView mList;
	private FlightHistogramAdapter mAdapter;
	private int mColWidth = 0;
	private IFlightHistogramListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, IFlightHistogramListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mList = (ListView) inflater.inflate(R.layout.fragment_tablet_results_flight_histogram, container, false);

		// Adapter setup
		if (mAdapter == null) {
			mAdapter = new FlightHistogramAdapter(getActivity());
		}
		if (Db.getFlightSearchHistogramResponse() != null) {
			mAdapter.setHistogramData(Db.getFlightSearchHistogramResponse());
			mAdapter.setColWidth(mColWidth);
		}
		mList.setAdapter(mAdapter);

		return mList;
	}

	@Override
	public void onResume() {
		super.onResume();

		mList.getViewTreeObserver().addOnPreDrawListener(mColWidthListener);

		if (mAdapter != null && mColWidth != 0) {
			mAdapter.setColWidth(mColWidth);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mList.getViewTreeObserver().removeOnPreDrawListener(mColWidthListener);
	}


	private ViewTreeObserver.OnPreDrawListener mColWidthListener = new ViewTreeObserver.OnPreDrawListener() {
		@Override
		public boolean onPreDraw() {
			if (mList != null && mList.getWidth() > 0 && mList.getWidth() != mColWidth) {
				setColWidth(mList.getWidth());
			}
			return true;
		}
	};


	public void setHistogramData(FlightSearchHistogramResponse data) {
		if (mAdapter != null && data != null) {
			mAdapter.setHistogramData(data);
			mAdapter.notifyDataSetChanged();
		}
	}

	public void setColWidth(int width) {
		if (mAdapter != null) {
			mAdapter.setColWidth(width);
		}
		else {
			mColWidth = width;
		}
	}

	@Override
	public void onListItemClick(ListView list, View view, int pos, long id) {
		if (list == mList && list.getAdapter() != null && list.getAdapter() == mAdapter) {
			FlightHistogram histo = mAdapter.getItem(pos);
			if (histo != null) {
				mListener.onGdeDateSelected(histo.getDate());
			}
		}
	}

}
