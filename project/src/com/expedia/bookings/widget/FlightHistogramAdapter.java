package com.expedia.bookings.widget;

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

// TODO better edge case handling, i.e if result set is small (less than 3)
public class FlightHistogramAdapter extends BaseAdapter {

	private Context mContext;

	private FlightSearchHistogramResponse mFlightHistogramResponse;

	private int mColWidth;

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

	private int mMaxDateWidth;
	private int mMaxPriceWidth;

	private void processData() {
		if (mFlightHistogramResponse.getFlightHistograms() == null) {
			return;
		}
		// Let us measure some things
		ViewGroup row = Ui.inflate(mContext, R.layout.row_flight_histogram, null);
		final TextView dateTv = Ui.findView(row, R.id.flight_histogram_date);
		final TextView priceTv = Ui.findView(row, R.id.flight_histogram_price);

		mMaxDateWidth = 0;
		mMaxPriceWidth = 0;
		for (FlightHistogram gram : mFlightHistogramResponse.getFlightHistograms()) {
			dateTv.setText(sDateFormatter.print(gram.getDate()));
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

	@Override
	public int getCount() {
		if (mFlightHistogramResponse.getFlightHistograms() != null) {
			return mFlightHistogramResponse.getFlightHistograms().size();
		}
		return 0;
	}

	@Override
	public FlightHistogram getItem(int position) {
		if (mFlightHistogramResponse.getFlightHistograms() != null) {
			mFlightHistogramResponse.getFlightHistograms().get(position);
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

		FlightHistogram gram = mFlightHistogramResponse.getFlightHistograms().get(position);

		dateTv.setText(sDateFormatter.print(gram.getDate()));
		priceTv.setText(gram.getPriceAsStr());
		priceTv.setBackgroundColor(mContext.getResources().getColor(mFlightHistogramResponse.getColorResIdForPrice(gram)));

		// relative width
		double minPrice = mFlightHistogramResponse.getMinPrice();
		double maxPrice = mFlightHistogramResponse.getMaxPrice();
		double barPerc = (gram.getMinPrice() - minPrice) / (maxPrice - minPrice);
		int extraWidthToAdd = mColWidth - mMaxPriceWidth - mMaxDateWidth;
		int totalDateWidth = mMaxDateWidth + ((int) (extraWidthToAdd * barPerc));

		dateTv.setWidth(totalDateWidth);
		priceTv.setWidth(mMaxPriceWidth);

		return row;
	}

}
