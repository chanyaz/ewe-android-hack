package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightHistogram;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.WeeklyFlightHistogram;
import com.expedia.bookings.utils.Ui;

public class WeeklyFlightHistogramAdapter extends BaseAdapter {

	private FlightSearchHistogramResponse mFlightHistogramResponse;

	List<FlightHistogram> mHistograms = null;
	List<WeeklyFlightHistogram> mWeeklyHistograms = null;

	private LocalDate mSelectedDepartureDate;

	public WeeklyFlightHistogramAdapter() {
	}

	public void setHistogramData(FlightSearchHistogramResponse histogramResponse) {
		mFlightHistogramResponse = histogramResponse;
		notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		mHistograms = null;
		mWeeklyHistograms = null;
		processData();
		super.notifyDataSetChanged();
	}

	public void setSelectedDepartureDate(LocalDate date) {
		mSelectedDepartureDate = date;
		notifyDataSetChanged();
	}

	private void processData() {
		if (mFlightHistogramResponse == null || mFlightHistogramResponse.getFlightHistograms() == null) {
			return;
		}

		getWeeklyHistograms();
	}

	private List<WeeklyFlightHistogram> getWeeklyHistograms() {
		if (mWeeklyHistograms == null) {
			mWeeklyHistograms = new ArrayList<>();

			mHistograms = getHistograms();

			if (mHistograms != null && mHistograms.size() > 0) {
				LocalDate startDate = LocalDate.now();
				LocalDate endDate = mHistograms.get(mHistograms.size() - 1).getKeyDate();

				WeeklyFlightHistogram current = new WeeklyFlightHistogram(startDate);
				mWeeklyHistograms.add(current);
				for (FlightHistogram gram : mHistograms) {
					if (mSelectedDepartureDate != null && gram.getKeyDate().isBefore(mSelectedDepartureDate)) {
						continue;
					}
					if (gram.getKeyDate().isBefore(current.getWeekStart())) {
						continue;
					}
					while (current.getWeekEnd().isBefore(gram.getKeyDate())) {
						current = new WeeklyFlightHistogram(current.getWeekEnd().plusDays(1));
						mWeeklyHistograms.add(current);
					}
					current.add(gram);
				}
				while (endDate.isAfter(current.getWeekEnd())) {
					current = new WeeklyFlightHistogram(current.getWeekEnd().plusDays(1));
					mWeeklyHistograms.add(current);
				}
			}
		}
		return mWeeklyHistograms;
	}

	/**
	 * Returns the FlightHistogramResponse's histograms. If an outbound flight date is selected,
	 * this will return the inbound flight date histograms instead.
	 *
	 * @return
	 */
	private List<FlightHistogram> getHistograms() {
		if (mHistograms != null) {
			return mHistograms;
		}

		if (mFlightHistogramResponse == null) {
			// Return null in this case because the data isn't available yet
			return null;
		}

		if (mFlightHistogramResponse.getFlightHistograms() == null) {
			return new ArrayList<>();
		}

		return mFlightHistogramResponse.getFlightHistograms();
	}

	@Override
	public int getCount() {
		getWeeklyHistograms();
		return mWeeklyHistograms == null ? 0 : mWeeklyHistograms.size();
	}

	@Override
	public WeeklyFlightHistogram getItem(int position) {
		getWeeklyHistograms();
		return mWeeklyHistograms == null
			|| position >= mWeeklyHistograms.size()
			? null
			: mWeeklyHistograms.get(position);
	}

	// Finds the position of the date within the weeks in this adapter
	public int getPositionOf(LocalDate localDate) {
		getWeeklyHistograms();
		if (mWeeklyHistograms == null) {
			return -1;
		}
		for (int i = 0; i < mWeeklyHistograms.size(); i++) {
			WeeklyFlightHistogram week = mWeeklyHistograms.get(i);
			if (week.isInWeek(localDate)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getWeekStart().toDateTimeAtStartOfDay().getMillis();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = Ui.inflate(R.layout.row_weekly_flight_histogram, parent, false);
		}
		View row = convertView;
		StackedDateRangeTextView dateTv = Ui.findView(row, R.id.date);
		HistogramView histogram = Ui.findView(row, R.id.histogram);

		WeeklyFlightHistogram week = getItem(position);

		if (week != null) {
			dateTv.setDates(week.getWeekStart(), week.getWeekEnd());

			// relative width
			histogram.setData(mFlightHistogramResponse.getMinPrice(), mFlightHistogramResponse.getMaxPrice(), week);
		}
		return row;
	}

}
