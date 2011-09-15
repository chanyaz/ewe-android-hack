package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.android.util.ViewUtils;

public class RoomsAndRatesAdapter extends BaseAdapter {

	// When to start showing "only X rooms left!" message in layout
	private static final int ROOMS_LEFT_CUTOFF = 5;

	private Context mContext;
	private Resources mResources;

	private LayoutInflater mInflater;

	private List<Rate> mRates;

	private List<String> mValueAdds;

	private float mSaleTextSize;

	public RoomsAndRatesAdapter(Context context, AvailabilityResponse response) {
		mContext = context;
		mResources = context.getResources();

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mRates = response.getRates();

		// Calculate the individual value-adds for each row, based on the common value-adds
		List<String> common = response.getCommonValueAdds();
		mValueAdds = new ArrayList<String>(mRates.size());
		for (int a = 0; a < mRates.size(); a++) {
			Rate rate = mRates.get(a);
			List<String> unique = new ArrayList<String>(rate.getValueAdds());
			if (common != null) {
				unique.removeAll(common);
			}
			if (unique.size() > 0) {
				mValueAdds.add(context.getString(R.string.value_add_template,
						FormatUtils.series(context, unique, ",", null)));
			}
			else {
				mValueAdds.add(null);
			}
		}

		// Calculate the size of the sale text size
		mSaleTextSize = ViewUtils.getTextSizeForMaxLines(context.getString(R.string.savings_template, 50.0), 2, 11,
				new TextPaint(), 28);
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
			holder.valueAddsLayout = (ViewGroup) convertView.findViewById(R.id.value_adds_layout);
			holder.valueAdds = (TextView) convertView.findViewById(R.id.value_adds_text_view);
			holder.valueAddsBeds = (TextView) convertView.findViewById(R.id.value_adds_beds_text_view);

			holder.saleLabel.setTextSize(mSaleTextSize);

			// #6966: Fix specifically for Android 2.1 and below.  Since RelativeLayout can't properly align
			// bottom on a ListView, I'm just going to add extra margin
			if (Build.VERSION.SDK_INT <= 7) {
				holder.beds.setPadding(0, (int) mContext.getResources().getDisplayMetrics().density * 24,
						(int) mContext.getResources().getDisplayMetrics().density * 10, 0);
			}

			convertView.setTag(holder);
		}
		else {
			holder = (RoomAndRateHolder) convertView.getTag();
		}

		Rate rate = (Rate) getItem(position);

		holder.description.setText(Html.fromHtml(rate.getRoomDescription()));
		holder.price.setText(StrUtils.formatHotelPrice(rate.getDisplayRate()));

		String explanation = "";

		// Check if there should be a strike-through rate, if this is on sale
		double savings = rate.getSavingsPercent();
		if (savings > 0) {
			explanation += "<strike>"
					+ StrUtils.formatHotelPrice(rate.getDisplayBaseRate())
					+ "</strike> ";

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
		if (!Rate.showInclusivePrices()) {
			if (rateBreakdown == null) {
				// If rateBreakdown is null, we assume that this is a per/night hotel
				explanationId = R.string.rate_per_night;
			}
			else if (rateBreakdown.size() > 1) {
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
			bedText += "\n" + mResources.getQuantityString(R.plurals.number_of_rooms_left, numRoomsLeft, numRoomsLeft);
		}

		// If there are value adds, setup the alternate view
		String valueAdds = mValueAdds.get(position);
		if (valueAdds == null) {
			holder.beds.setVisibility(View.VISIBLE);
			holder.valueAddsLayout.setVisibility(View.GONE);

			holder.beds.setText(bedText);
		}
		else {
			holder.beds.setVisibility(View.GONE);
			holder.valueAddsLayout.setVisibility(View.VISIBLE);

			holder.valueAdds.setText(mValueAdds.get(position));
			holder.valueAddsBeds.setText(bedText);
		}

		return convertView;
	}

	private static class RoomAndRateHolder {
		public TextView description;
		public TextView price;
		public TextView priceExplanation;
		public TextView beds;
		public ImageView saleImage;
		public TextView saleLabel;

		public ViewGroup valueAddsLayout;
		public TextView valueAdds;
		public TextView valueAddsBeds;
	}
}
