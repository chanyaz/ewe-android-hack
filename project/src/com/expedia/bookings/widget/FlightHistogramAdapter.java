package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightHistogram;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;

public class FlightHistogramAdapter extends BaseAdapter {

	private Context mContext;

	private List<FlightHistogram> mFlightHistograms;

	public FlightHistogramAdapter(Context context) {
		mContext = context;
	}

	public void setHistogramData(List<FlightHistogram> histograms) {
		mFlightHistograms = histograms;
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.row_flight_histogram, null);
		}

		FlightHistogram gram = mFlightHistograms.get(position);

		TextView dateTv = Ui.findView(convertView, R.id.flight_histogram_date);
		TextView priceTv = Ui.findView(convertView, R.id.flight_histogram_price);

		dateTv.setText(JodaUtils.formatLocalDate(mContext, gram.getDate(), JodaUtils.FLAGS_MEDIUM_DATE_FORMAT));
		dateTv.setTextColor(mContext.getResources().getColor(android.R.color.white));
		dateTv.setBackgroundColor(mContext.getResources().getColor(R.color.dark_blue));

		priceTv.setText(gram.getPriceAsStr());

		return convertView;
	}

}
