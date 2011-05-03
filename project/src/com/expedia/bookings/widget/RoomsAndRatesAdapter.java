package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.hotellib.data.Money;
import com.mobiata.hotellib.data.Rate;
import com.mobiata.hotellib.data.RateBreakdown;

public class RoomsAndRatesAdapter extends BaseAdapter {

	// When to start showing "only X rooms left!" message in layout
	private static final int ROOMS_LEFT_CUTOFF = 5;

	private Context mContext;

	private LayoutInflater mInflater;

	private List<Rate> mRates;

	public RoomsAndRatesAdapter(Context context, List<Rate> rates) {
		mContext = context;

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
			holder.saleImage = (ImageView) convertView.findViewById(R.id.sale_image_view);
			holder.saleLabel = (TextView) convertView.findViewById(R.id.sale_text_view);

			convertView.setTag(holder);
		}
		else {
			holder = (RoomAndRateHolder) convertView.getTag();
		}

		Rate rate = (Rate) getItem(position);

		holder.description.setText(Html.fromHtml(rate.getRoomDescription()));
		holder.price.setText(rate.getAverageRate().getFormattedMoney(Money.F_NO_DECIMAL));

		String explanation = "";

		// Check if there should be a strike-through rate, if this is on sale
		double savings = rate.getSavingsPercent();
		if (savings > 0) {
			explanation += "<strike>" + rate.getAverageBaseRate().getFormattedMoney(Money.F_NO_DECIMAL) + "</strike> ";

			holder.saleLabel.setText(mContext.getString(R.string.savings_template, savings * 100));
			holder.saleImage.setVisibility(View.VISIBLE);
			holder.saleLabel.setVisibility(View.VISIBLE);
		}
		else {
			holder.saleImage.setVisibility(View.GONE);
			holder.saleLabel.setVisibility(View.GONE);
		}

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
			explanation += mContext.getString(explanationId);
		}

		if (explanation.length() > 0) {
			holder.priceExplanation.setVisibility(View.VISIBLE);
			holder.priceExplanation.setText(Html.fromHtml(explanation, null, new StrikethroughTagHandler()));
		}
		else {
			holder.priceExplanation.setVisibility(View.GONE);
		}

		String bedText = rate.getRatePlanName();

		// If there are < ROOMS_LEFT_CUTOFF rooms left, show a warning to the user
		int numRoomsLeft = rate.getNumRoomsLeft();
		if (numRoomsLeft > 0 && numRoomsLeft <= ROOMS_LEFT_CUTOFF) {
			bedText += "\n";
			if (numRoomsLeft == 1) {
				bedText += mContext.getString(R.string.rooms_left_one);
			}
			else {
				bedText += mContext.getString(R.string.rooms_left_template, numRoomsLeft);
			}
		}

		holder.beds.setText(bedText);

		return convertView;
	}

	private static class RoomAndRateHolder {
		public TextView description;
		public TextView price;
		public TextView priceExplanation;
		public TextView beds;
		public ImageView saleImage;
		public TextView saleLabel;
	}
}
