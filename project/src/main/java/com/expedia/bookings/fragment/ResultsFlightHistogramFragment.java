package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.WeeklyFlightHistogram;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.WeeklyFlightHistogramAdapter;

/**
 * ResultsFlightHistogramFragment: The flight histogram fragment designed for tablet results 2014
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightHistogramFragment extends ListFragment {

	private ListView mList;
	private WeeklyFlightHistogramAdapter mAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mList = Ui.inflate(inflater, R.layout.fragment_tablet_results_flight_histogram, container, false);

		// Adapter setup
		if (mAdapter == null) {
			mAdapter = new WeeklyFlightHistogramAdapter();
		}
		setHistogramData(Db.getFlightSearchHistogramResponse());
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
			mList.smoothScrollToPosition(0);
		}
	}

	public void scrollToMonth(YearMonth yearMonth) {
		LocalDate firstOfThaMonth = new LocalDate(yearMonth.year().get(), yearMonth.monthOfYear().get(), 1);
		mList.smoothScrollToPosition(mAdapter.getPositionOf(firstOfThaMonth));
	}

	@Override
	public void onListItemClick(ListView list, View view, int pos, long id) {
		if (list == mList && list.getAdapter() != null && list.getAdapter() == mAdapter) {
			WeeklyFlightHistogram week = mAdapter.getItem(pos);
			if (week != null) {
				Events.post(new Events.GdeItemSelected(week));
			}
		}
	}

}
