package com.expedia.bookings.widget;

import java.util.List;

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

	private List<FlightHistogram> mFlightHistograms;
	private double mMinPrice;
	private double mMaxPrice;

	private int mColWidth;

	private static final DateTimeFormatter sDateFormatter = DateTimeFormat.forPattern("MMM d, E");

	public FlightHistogramAdapter(Context context) {
		mContext = context;
	}

	public void setHistogramData(FlightSearchHistogramResponse histogramResponse) {
		if (histogramResponse != null) {
			mFlightHistograms = histogramResponse.getFlightHistograms();
			mMinPrice = histogramResponse.getMinPrice();
			mMaxPrice = histogramResponse.getMaxPrice();
			processData();
			notifyDataSetChanged();
		}
	}

	public void setColWidth(int width) {
		mColWidth = width;
	}

	private int mMaxDateWidth;
	private int mMaxPriceWidth;

	private void processData() {
		if (mFlightHistograms == null) {
			return;
		}
		// Let us measure some things
		ViewGroup row = Ui.inflate(mContext, R.layout.row_flight_histogram, null);
		final TextView dateTv = Ui.findView(row, R.id.flight_histogram_date);
		final TextView priceTv = Ui.findView(row, R.id.flight_histogram_price);

		mMaxDateWidth = 0;
		mMaxPriceWidth = 0;
		for (FlightHistogram gram : mFlightHistograms) {
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
		if (mFlightHistograms != null) {
			return mFlightHistograms.size();
		}
		return 0;
	}

	@Override
	public FlightHistogram getItem(int position) {
		if (mFlightHistograms != null) {
			mFlightHistograms.get(position);
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

		FlightHistogram gram = mFlightHistograms.get(position);

		dateTv.setText(sDateFormatter.print(gram.getDate()));
		priceTv.setText(gram.getPriceAsStr());

		// relative width
		double barPerc = (gram.getMinPrice() - mMinPrice) / (mMaxPrice - mMinPrice);
		int extraWidthToAdd = mColWidth - mMaxPriceWidth - mMaxDateWidth;
		int totalDateWidth = mMaxDateWidth + ((int) (extraWidthToAdd * barPerc));

		dateTv.setWidth(totalDateWidth);
		priceTv.setWidth(mMaxPriceWidth);

		return row;
	}

}
