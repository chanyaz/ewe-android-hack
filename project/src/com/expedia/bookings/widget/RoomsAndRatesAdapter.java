package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.data.RateBreakdown;

public class RoomsAndRatesAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

	private List<Rate> mRates;

	public RoomsAndRatesAdapter(Context context, List<Rate> rates) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mRates = rates;
	}

	@Override
	public int getCount() {
		return mRates.size();
	}

	@Override
	public Object getItem(int position) {
		return mRates.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RoomAndRateHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_room_rate, parent, false);

			holder = new RoomAndRateHolder();
			holder.description = (TextView) convertView.findViewById(R.id.room_description_text_view);
			holder.price = (TextView) convertView.findViewById(R.id.price_text_view);
			holder.priceExplanation = (TextView) convertView.findViewById(R.id.price_explanation_text_view);
			holder.beds = (TextView) convertView.findViewById(R.id.beds_text_view);

			convertView.setTag(holder);
		}
		else {
			holder = (RoomAndRateHolder) convertView.getTag();
		}

		Rate rate = (Rate) getItem(position);

		holder.description.setText(rate.getRoomDescription());
		holder.price.setText(rate.getDailyAmountBeforeTax().getFormattedMoney());

		// Determine whether to show rate, rate per night, or avg rate per night for explanation
		int explanationId = 0;
		List<RateBreakdown> rateBreakdown = rate.getRateBreakdownList();
		if (rateBreakdown == null) {
			// If rateBreakdown is null, we assume that this is a per/night hotel
			explanationId = R.string.rate_per_night;
		}
		else {
			if (rateBreakdown.size() <= 1) {
				holder.priceExplanation.setVisibility(View.GONE);
			}
			else {
				if (rate.rateChanges()) {
					explanationId = R.string.rate_avg_per_night;
				}
				else {
					explanationId = R.string.rate_per_night;
				}
			}
		}

		if (explanationId != 0) {
			holder.priceExplanation.setVisibility(View.VISIBLE);
			holder.priceExplanation.setText(explanationId);
		}
		else {
			holder.priceExplanation.setVisibility(View.GONE);
		}

		holder.beds.setText(rate.getRatePlanName());

		return convertView;
	}

	private static class RoomAndRateHolder {
		public TextView description;
		public TextView price;
		public TextView priceExplanation;
		public TextView beds;
	}
}
