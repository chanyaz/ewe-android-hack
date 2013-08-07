package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.text.StrikethroughTagHandler;

public class RoomsAndRatesAdapter extends BaseAdapter {

	// We implement a selected state for rows here.  The reason for this is that in touch mode, a ListView
	// does not have a "selected" state (unless you are in touch mode).
	private static final int ROW_NORMAL = 0;
	private static final int ROW_SELECTED = 1;

	// When to start showing "only X rooms left!" message in layout
	private static final int ROOMS_LEFT_CUTOFF = 5;

	private Context mContext;
	private Resources mResources;

	private LayoutInflater mInflater;

	private Property mProperty;
	private List<Rate> mRates;

	private List<CharSequence> mValueAdds;

	private float mSaleTextSize;

	private int mSelectedPosition = -1;
	private boolean mHighlightSelectedPosition = true;

	private int mBedSalePadding;

	private StringBuilder mBuilder;

	public RoomsAndRatesAdapter(Context context, HotelOffersResponse response) {
		mContext = context;
		mResources = context.getResources();

		mInflater = LayoutInflater.from(context);

		mProperty = response.getProperty();
		mRates = response.getRates();

		// #12669: Somehow we're getting back null rates.  At the very least, we just want to show *no* results
		// rather than a crash.  Easiest to just have an empty List to indicate zero results.
		if (mRates == null) {
			mRates = new ArrayList<Rate>();
		}

		// Calculate the individual value-adds for each row, based on the common value-adds
		List<String> common = response.getCommonValueAdds();
		mValueAdds = new ArrayList<CharSequence>(mRates.size());
		for (int a = 0; a < mRates.size(); a++) {
			Rate rate = mRates.get(a);
			List<String> unique = new ArrayList<String>(rate.getValueAdds());
			if (common != null) {
				unique.removeAll(common);
			}
			if (unique.size() > 0) {
				mValueAdds.add(Html.fromHtml(context.getString(R.string.value_add_template,
						FormatUtils.series(context, unique, ",", null))));
			}
			else {
				mValueAdds.add(null);
			}
		}

		// Calculate the size of the sale text size
		mSaleTextSize = LayoutUtils.getSaleTextSize(context);

		mBedSalePadding = (int) Math.round(mResources.getDisplayMetrics().density * 26);

		mBuilder = new StringBuilder();
	}

	public void highlightSelectedPosition(boolean highlight) {
		mHighlightSelectedPosition = highlight;
	}

	public void setSelectedPosition(int selectedPosition) {
		mSelectedPosition = selectedPosition;
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
	public int getItemViewType(int position) {
		if (position == mSelectedPosition && mHighlightSelectedPosition) {
			return ROW_SELECTED;
		}
		else {
			return ROW_NORMAL;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
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
			holder.saleLabel = (TextView) convertView.findViewById(R.id.sale_text_view);
			holder.valueAddsLayout = (ViewGroup) convertView.findViewById(R.id.value_adds_layout);
			holder.valueAdds = (TextView) convertView.findViewById(R.id.value_adds_text_view);
			holder.valueAddsBeds = (TextView) convertView.findViewById(R.id.value_adds_beds_text_view);

			holder.saleLabel.setTextSize(mSaleTextSize);

			convertView.setTag(holder);
		}
		else {
			holder = (RoomAndRateHolder) convertView.getTag();
		}

		Rate rate = (Rate) getItem(position);

		holder.description.setText(Html.fromHtml(rate.getRoomDescription()));
		holder.price.setText(StrUtils.formatHotelPrice(rate.getDisplayRate()));

		mBuilder.setLength(0);

		// Check if there should be a strike-through rate, if this is on sale
		if (rate.isOnSale()) {
			mBuilder.append(mContext.getString(R.string.strike_template,
					StrUtils.formatHotelPrice(rate.getDisplayBaseRate())));
			mBuilder.append(' ');

			holder.saleLabel.setText(mContext.getString(R.string.percent_off_template, rate.getDiscountPercent()));
			holder.saleLabel.setVisibility(View.VISIBLE);
		}
		else {
			holder.saleLabel.setVisibility(View.GONE);
		}

		// Determine whether to show rate, rate per night, or avg rate per night for explanation
		int explanationId = rate.getQualifier();
		if (explanationId != 0) {
			mBuilder.append(mContext.getString(explanationId));
		}

		if (mBuilder.length() > 0) {
			holder.priceExplanation.setVisibility(View.VISIBLE);
			holder.priceExplanation.setText(Html.fromHtml(mBuilder.toString(), null, new StrikethroughTagHandler()));
		}
		else {
			holder.priceExplanation.setVisibility(View.GONE);
		}

		// We adjust the bed's padding based on whether the sale tag is visible or not
		int padding;
		if (rate.isOnSale()) {
			padding = mBedSalePadding;
			//1747. VSC Change price text to sale color
			if (ExpediaBookingApp.IS_VSC) {
				holder.price.setTextColor(Ui.obtainThemeColor((Activity)mContext, R.attr.hotelPriceSaleColor));
			}
		}
		else {
			if (Build.VERSION.SDK_INT > 7) {
				padding = 0;
			}
			else {
				// We have to do some special acrobatics here because the beds view won't properly align
				// to othe bottom on API 7 or less.
				padding = (holder.priceExplanation.getVisibility() == View.VISIBLE) ? mBedSalePadding : (int) Math
						.round(mResources.getDisplayMetrics().density * 10);
			}
		}

		holder.beds.setPadding(holder.beds.getPaddingLeft(), padding, holder.beds.getPaddingRight(),
				holder.beds.getPaddingBottom());

		mBuilder.setLength(0);

		if (shouldShowBedDescription(rate)) {
			addBedRow(rate.getRatePlanName());
		}

		if (shouldShowNonRefundable(rate)) {
			addBedRow(mResources.getString(R.string.non_refundable));
		}

		if (rate.shouldShowFreeCancellation()) {
			addBedRow(mResources.getString(R.string.free_cancellation));
		}

		// If there are < ROOMS_LEFT_CUTOFF rooms left, show a warning to the user
		// however non-refundable trumps this text
		// 1400. VSC - remove urgency messages throughout the app
		if (shouldShowRoomsLeft(rate) && !shouldShowNonRefundable(rate) && !ExpediaBookingApp.IS_VSC) {
			int numRoomsLeft = rate.getNumRoomsLeft();
			addBedRow(mResources.getQuantityString(R.plurals.number_of_rooms_left, numRoomsLeft, numRoomsLeft));
		}

		if (mBuilder.indexOf("\n") > 0) {
			// move the sale label up so as to accomodate the multiple lines for the bed text
			((RelativeLayout.LayoutParams) holder.saleLabel.getLayoutParams()).addRule(RelativeLayout.CENTER_VERTICAL, 0);
			((RelativeLayout.LayoutParams) holder.saleLabel.getLayoutParams()).topMargin = (int) mContext.getResources().getDimension(R.dimen.margin_top_sale_ribbon_room_rates);
		}
		else {
			((RelativeLayout.LayoutParams) holder.saleLabel.getLayoutParams()).addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		}

		// If there are value adds, setup the alternate view
		CharSequence valueAdds = mValueAdds.get(position);
		if (valueAdds == null) {
			holder.beds.setVisibility(View.VISIBLE);
			holder.valueAddsLayout.setVisibility(View.GONE);

			holder.beds.setText(mBuilder.toString());
		}
		else {
			holder.beds.setVisibility(View.GONE);
			holder.valueAddsLayout.setVisibility(View.VISIBLE);

			holder.valueAdds.setText(mValueAdds.get(position));
			holder.valueAddsBeds.setText(mBuilder.toString());
		}

		// Set the background based on whether the row is selected or not
		if (getItemViewType(position) == ROW_SELECTED) {
			convertView.setBackgroundResource(R.drawable.bg_row_selected);
		}
		else {
			convertView.setBackgroundResource(R.drawable.bg_row);
		}

		return convertView;
	}

	private boolean shouldShowBedDescription(Rate rate) {
		return mProperty.isMerchant();
	}

	private boolean shouldShowNonRefundable(Rate rate) {
		return rate.isNonRefundable();
	}

	private boolean shouldShowRoomsLeft(Rate rate) {
		int numRoomsLeft = rate.getNumRoomsLeft();
		return numRoomsLeft > 0 && numRoomsLeft <= ROOMS_LEFT_CUTOFF;
	}

	private void addBedRow(String str) {
		if (mBuilder.length() > 0) {
			mBuilder.append('\n');
		}
		mBuilder.append(str);
	}

	private static class RoomAndRateHolder {
		public TextView description;
		public TextView price;
		public TextView priceExplanation;
		public TextView beds;
		public TextView saleLabel;

		public ViewGroup valueAddsLayout;
		public TextView valueAdds;
		public TextView valueAddsBeds;
	}
}
