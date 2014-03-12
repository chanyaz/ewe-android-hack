package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightHistogramAdapter;
import com.expedia.bookings.widget.TextView;

/**
 * ResultsFlightHistogramFragment: The flight histogram fragment designed for tablet results 2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightHistogramFragment extends ListFragment {

	private FlightHistogramAdapter mAdapter;

	private ProgressBar mProgressBar;

	private HistogramFragmentListener mListener;

	private boolean mShowProgress = false;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = Ui.findFragmentListener(this, HistogramFragmentListener.class, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_flight_histogram, container, false);

		// Adapter setup
		if (mAdapter == null) {
			mAdapter = new FlightHistogramAdapter(getActivity());
		}
		if (Db.getFlightSearchHistogramResponse() != null) {
			mAdapter.setHistogramData(Db.getFlightSearchHistogramResponse().getFlightHistograms());
		}
		ListView lv = Ui.findView(view, android.R.id.list);
		lv.setAdapter(mAdapter);

		TextView headerTv = Ui.findView(view, R.id.flight_histogram_header);
		headerTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onHeaderClick();
				}
			}
		});

		mProgressBar = Ui.findView(view, R.id.flight_histogram_progress_bar);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		setShowProgressBar(mShowProgress);
		mShowProgress = false;
	}

	public void setHistogramData(FlightSearchHistogramResponse data) {
		if (mAdapter != null && data != null) {
			mAdapter.setHistogramData(data.getFlightHistograms());
			mAdapter.notifyDataSetChanged();
		}
	}

	public void setShowProgressBar(boolean show) {
		if (mProgressBar != null) {
			mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
		}
		else {
			// pick this up after onCreateView
			mShowProgress = show;
		}
	}

	public interface HistogramFragmentListener {
		void onHeaderClick();
	}
}
