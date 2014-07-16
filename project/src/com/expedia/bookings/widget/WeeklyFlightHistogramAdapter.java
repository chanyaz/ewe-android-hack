package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightHistogram;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.WeeklyFlightHistogram;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class WeeklyFlightHistogramAdapter extends BaseAdapter {

	private Context mContext;

	private FlightSearchHistogramResponse mFlightHistogramResponse;

	List<FlightHistogram> mHistograms = null;
	List<WeeklyFlightHistogram> mWeeklyHistograms = null;

	private int mSelectedDeparturePosition = -1;
	private LocalDate mSelectedDepartureDate;

	public WeeklyFlightHistogramAdapter(Context context) {
		mContext = context;
	}

	public void setHistogramData(FlightSearchHistogramResponse histogramResponse) {
		mFlightHistogramResponse = histogramResponse;
		notifyDataSetChanged();
	}

	private void unSetSelectedDeparturePosition() {
		if (mSelectedDeparturePosition != -1) {
			mSelectedDeparturePosition = -1;
			notifyDataSetChanged();
		}
	}

	@Override
	public void notifyDataSetChanged() {
		mHistograms = null;
		mWeeklyHistograms = null;
		processData();
		super.notifyDataSetChanged();
	}

	public void setSelectedDepartureDate(LocalDate date) {
		// Find the selected position
		if (date != null) {
			mSelectedDepartureDate = null;
			mHistograms = null;
			List<FlightHistogram> grams = getHistograms();
			mSelectedDepartureDate = date;
			if (grams != null) {
				for (int i = 0; i < grams.size(); i++) {
					if (grams.get(i).getKeyDate().equals(date)) {
						if (mSelectedDeparturePosition != i) {
							mSelectedDeparturePosition = i;
							notifyDataSetChanged();
						}
						return;
					}
				}
			}
		}
		mSelectedDepartureDate = date;
		unSetSelectedDeparturePosition();

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

			getHistograms();

			if (mHistograms != null) {
				WeeklyFlightHistogram current = null;
				for (FlightHistogram gram : mHistograms) {
					if (current == null || !current.isInWeek(gram)) {
						current = new WeeklyFlightHistogram(gram);
						mWeeklyHistograms.add(current);
					}
					else {
						current.add(gram);
					}
				}
			}
		}
		return mWeeklyHistograms;
	}

	private List<FlightHistogram> getHistograms() {
		if (mHistograms == null) {
			List<FlightHistogram> grams = null;
			if (mFlightHistogramResponse != null && mFlightHistogramResponse.getFlightHistograms() != null) {
				grams = mFlightHistogramResponse.getFlightHistograms();
				if (mSelectedDepartureDate != null) {
					if (mSelectedDeparturePosition >= 0
						&& grams.size() > mSelectedDeparturePosition
						&& grams.get(mSelectedDeparturePosition) != null
						&& grams.get(mSelectedDeparturePosition).getReturnFlightDateHistograms() != null) {
						grams = new ArrayList(grams
							.get(mSelectedDeparturePosition)
							.getReturnFlightDateHistograms()
							.values());
						// They naturally sort based on keydate
						Collections.sort(grams);
					}
					else {
						grams = new ArrayList<>();
					}
				}
			}
			mHistograms = grams;
		}
		return mHistograms;
	}

	@Override
	public int getCount() {
		getWeeklyHistograms();
		if (mWeeklyHistograms != null) {
			return mWeeklyHistograms.size();
		}
		return 0;
	}

	@Override
	public WeeklyFlightHistogram getItem(int position) {
		getWeeklyHistograms();
		if (mWeeklyHistograms != null && mWeeklyHistograms.size() > 0 && mWeeklyHistograms.size() > position) {
			return mWeeklyHistograms.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
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
			float minPrice = (float)mFlightHistogramResponse.getMinPrice();
			float maxPrice = (float)mFlightHistogramResponse.getMaxPrice();
			histogram.setData(minPrice, maxPrice, week);
		}
		return row;
	}

}
