package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightHistogram;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.utils.Ui;

public class FlightHistogramAdapter extends BaseAdapter {

	private Context mContext;

	private FlightSearchHistogramResponse mFlightHistogramResponse;

	private int mMaxDateWidth;
	private int mMaxPriceWidth;
	private int mColWidth;
	private int mSelectedDeparturePosition = -1;
	private LocalDate mSelectedDepartureDate;

	private static final DateTimeFormatter sDateFormatter = DateTimeFormat.forPattern("MMM d, E");

	public FlightHistogramAdapter(Context context) {
		mContext = context;
	}

	public void setHistogramData(FlightSearchHistogramResponse histogramResponse) {
		mFlightHistogramResponse = histogramResponse;

		processData();
		notifyDataSetChanged();
	}

	public void setColWidth(int width) {
		mColWidth = width;
	}

	private void unSetSelectedDeparturePosition() {
		if (mSelectedDeparturePosition != -1) {
			mSelectedDeparturePosition = -1;
			processData();
			notifyDataSetChanged();
		}
	}

	public void setSelectedDepartureDate(LocalDate date) {
		//Find the selected position
		if (date != null) {
			mSelectedDepartureDate = null;
			List<FlightHistogram> grams = getHistograms();
			mSelectedDepartureDate = date;
			if (grams != null) {
				for (int i = 0; i < grams.size(); i++) {
					if (grams.get(i).getKeyDate().equals(date)) {
						if (mSelectedDeparturePosition != i) {
							mSelectedDeparturePosition = i;
							processData();
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
		// Let us measure some things
		ViewGroup row = Ui.inflate(mContext, R.layout.row_flight_histogram, null);
		final TextView dateTv = Ui.findView(row, R.id.flight_histogram_date);
		final TextView priceTv = Ui.findView(row, R.id.flight_histogram_price);

		mMaxDateWidth = 0;
		mMaxPriceWidth = 0;

		Iterable<FlightHistogram> grams = getHistograms();
		if (grams != null) {
			for (FlightHistogram gram : getHistograms()) {
				dateTv.setText(sDateFormatter.print(gram.getKeyDate()));
				priceTv.setText(gram.getPriceAsStr());

				dateTv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
				priceTv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

				if (dateTv.getMeasuredWidth() > mMaxDateWidth) {
					mMaxDateWidth = dateTv.getMeasuredWidth();
				}
				if (priceTv.getMeasuredWidth() > mMaxPriceWidth) {
					mMaxPriceWidth = priceTv.getMeasuredWidth();
				}
			}
		}
	}

	private List<FlightHistogram> getHistograms() {
		List<FlightHistogram> grams = null;
		if (mFlightHistogramResponse != null && mFlightHistogramResponse.getFlightHistograms() != null) {
			if (mSelectedDepartureDate != null) {
				if (mSelectedDeparturePosition >= 0
					&& mFlightHistogramResponse.getFlightHistograms().size() > mSelectedDeparturePosition) {
					grams = new ArrayList(mFlightHistogramResponse.getFlightHistograms().get(mSelectedDeparturePosition)
						.getReturnFlightDateHistograms().values());
					//They sort based on keydate
					Collections.sort(grams);
				}
			}
			else {
				grams = mFlightHistogramResponse.getFlightHistograms();
			}
		}
		return grams;
	}

	@Override
	public int getCount() {
		List<FlightHistogram> grams = getHistograms();
		if (grams != null) {
			return grams.size();
		}
		return 0;
	}

	@Override
	public FlightHistogram getItem(int position) {
		List<FlightHistogram> grams = getHistograms();
		if (grams != null && grams.size() > 0 && grams.size() > position) {
			return grams.get(position);
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.row_flight_histogram, parent, false);
		}
		View row = convertView;
		TextView dateTv = Ui.findView(row, R.id.flight_histogram_date);
		TextView priceTv = Ui.findView(row, R.id.flight_histogram_price);

		FlightHistogram gram = getItem(position);

		if (gram != null) {
			dateTv.setText(sDateFormatter.print(gram.getKeyDate()));
			priceTv.setText(gram.getPriceAsStr());
			priceTv
				.setBackgroundColor(
					mContext.getResources().getColor(mFlightHistogramResponse.getColorResIdForPrice(gram)));

			// relative width
			double minPrice = mFlightHistogramResponse.getMinPrice();
			double maxPrice = mFlightHistogramResponse.getMaxPrice();

			double barPerc = maxPrice == minPrice ? 1f : (gram.getMinPrice() - minPrice) / (maxPrice - minPrice);
			int extraWidthToAdd = mColWidth - mMaxPriceWidth - mMaxDateWidth;
			int totalDateWidth = mMaxDateWidth + ((int) (extraWidthToAdd * barPerc));

			dateTv.setWidth(totalDateWidth);
			priceTv.setWidth(mMaxPriceWidth);
		}
		return row;
	}

}
