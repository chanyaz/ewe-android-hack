package com.expedia.bookings.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.BedType;
import com.expedia.bookings.data.Rate.BedTypeId;
import com.expedia.bookings.widget.SummarizedRoomRates;

public class AvailabilitySummaryLayoutUtils {

	public static void updateSummarizedRates(Context context, Property property,
			AvailabilityResponse availabilityResponse, View view, String buttonText,
			OnClickListener buttonOnClickListener, OnRateClickListener onRateClickListener) {

		// view is not created yet, so there's nothing to do here
		if (view == null) {
			return;
		}

		View emptyAvailabilitySummaryTextView = view.findViewById(R.id.empty_summart_container);
		ProgressBar ratesProgressBar = (ProgressBar) view.findViewById(R.id.rates_progress_bar);

		TextView selectRoomButton = (TextView) view.findViewById(R.id.book_now_button);
		selectRoomButton.setText(buttonText);
		selectRoomButton.setOnClickListener(buttonOnClickListener);

		if (availabilityResponse != null) {
			layoutAvailabilitySummary(context, property, availabilityResponse.getSummarizedRoomRates(), view,
					onRateClickListener);
			emptyAvailabilitySummaryTextView.setVisibility(View.GONE);
			ratesProgressBar.setVisibility(View.GONE);
		}
		else {
			// since the data is not yet available,
			// make sure to clean out any old data and show the loading screen
			showLoadingForRates(context, view);
		}
	}

	private static final int MAX_SUMMARIZED_RATE_RESULTS = 3;
	private static final int ANIMATION_SPEED = 350;

	public static void setupAvailabilitySummary(Context context, Property property, View view) {

		// view is not created yet, so there's nothing to do here
		if (view == null) {
			return;
		}

		View availabilitySummaryContainer = view.findViewById(R.id.availability_summary_container);

		boolean isPropertyOnSale = property.getLowestRate().getSavingsPercent() > 0;
		if (isPropertyOnSale) {
			availabilitySummaryContainer.setBackgroundResource(R.drawable.bg_summarized_room_rates_sale);
		}
		else {
			availabilitySummaryContainer.setBackgroundResource(R.drawable.bg_summarized_room_rates);
		}

		View minPriceRow = view.findViewById(R.id.min_price_row_container);
		TextView minPrice = (TextView) minPriceRow.findViewById(R.id.min_price_text_view);

		String displayRateString = StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate());
		String minPriceString = context.getString(R.string.min_room_price_template, displayRateString);
		int startingIndexOfDisplayRate = minPriceString.indexOf(displayRateString);

		// style the minimum available price text
		StyleSpan textStyleSpan = new StyleSpan(Typeface.BOLD);
		Resources r = context.getResources();
		ForegroundColorSpan textColorSpan = new ForegroundColorSpan(r.getColor(R.color.hotel_price_text_color));
		ForegroundColorSpan textWhiteColorSpan = new ForegroundColorSpan(r.getColor(android.R.color.white));
		ForegroundColorSpan textBlackColorSpan = new ForegroundColorSpan(r.getColor(android.R.color.black));

		Spannable str = new SpannableString(minPriceString);

		str.setSpan(textStyleSpan, 0, minPriceString.length(), 0);

		if (isPropertyOnSale) {
			str.setSpan(textWhiteColorSpan, 0, minPriceString.length(), 0);
		}
		else {
			str.setSpan(textColorSpan, startingIndexOfDisplayRate,
					startingIndexOfDisplayRate + displayRateString.length(), 0);
			str.setSpan(textBlackColorSpan, 0, startingIndexOfDisplayRate - 1, 0);
		}

		minPrice.setText(str);

		TextView perNighTextView = (TextView) minPriceRow.findViewById(R.id.per_night_text_view);
		perNighTextView.setTextColor(isPropertyOnSale ? r.getColor(android.R.color.white)
				: r.getColor(android.R.color.black));

		if (Rate.showInclusivePrices()) {
			perNighTextView.setVisibility(View.GONE);
		}
		else {
			perNighTextView.setVisibility(View.VISIBLE);
		}
	}

	private static void layoutAvailabilitySummary(Context context, Property property,
			SummarizedRoomRates summarizedRoomRates, View view, final OnRateClickListener onRateClickListener) {

		// view is not created yet, so there's nothing to do here
		if (view == null) {
			return;
		}

		ViewGroup availabilityRatesContainer = (ViewGroup) view.findViewById(R.id.rates_container);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		boolean isPropertyOnSale = property.getLowestRate().getSavingsPercent() > 0;
		availabilityRatesContainer.removeAllViews();

		// first adding all rows since the rows will exist regardless of whether
		// there are enough rooms available or not 
		for (int i = 0; i < MAX_SUMMARIZED_RATE_RESULTS; i++) {
			View summaryRow = inflater.inflate(R.layout.snippet_availability_summary_row, null);
			setHeightOfWeightOneForRow(summaryRow);

			if (i == (MAX_SUMMARIZED_RATE_RESULTS - 1)) {
				summaryRow.findViewById(R.id.divider).setVisibility(View.GONE);
			}
			availabilityRatesContainer.addView(summaryRow);
		}

		for (int i = 0; i < MAX_SUMMARIZED_RATE_RESULTS; i++) {
			View summaryRow = availabilityRatesContainer.getChildAt(i);
			ObjectAnimator animator = ObjectAnimator.ofFloat(summaryRow, "alpha", 0, 1);
			animator.setDuration(ANIMATION_SPEED);
			animator.start();

			if (i > (summarizedRoomRates.numSummarizedRates() - 1)) {
				continue;
			}

			final Rate rate = summarizedRoomRates.getRate(i);

			summaryRow.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					onRateClickListener.onRateClick(rate);
				}
			});

			View chevron = summaryRow.findViewById(R.id.availability_chevron_image_view);
			chevron.setVisibility(View.VISIBLE);

			TextView summaryDescription = (TextView) summaryRow.findViewById(R.id.availability_description_text_view);
			TextView priceTextView = (TextView) summaryRow.findViewById(R.id.availability_summary_price_text_view);

			Pair<BedTypeId, Rate> pair = summarizedRoomRates.getBedTypeToRatePair(i);
			for (BedType bedType : pair.second.getBedTypes()) {
				if (bedType.bedTypeId == pair.first) {
					summaryDescription.setText(Html.fromHtml(context.getString(R.string.bed_type_start_value_template,
							bedType.bedTypeDescription)));
					break;
				}
			}

			priceTextView.setText(StrUtils.formatHotelPrice(pair.second.getDisplayRate()));
			if (isPropertyOnSale) {
				priceTextView.setTextColor(context.getResources().getColor(R.color.hotel_price_sale_text_color));
			}
			else {
				priceTextView.setTextColor(context.getResources().getColor(R.color.hotel_price_text_color));
			}
		}
	}

	public interface OnRateClickListener {
		public void onRateClick(Rate rate);
	}

	public static void showLoadingForRates(Context context, View view) {

		// view is not created yet, so there's nothing to do here
		if (view == null) {
			return;
		}
		TextView emptyAvailabilitySummaryTextView = (TextView) view.findViewById(R.id.empty_summart_container);
		ProgressBar ratesProgressBar = (ProgressBar) view.findViewById(R.id.rates_progress_bar);
		View availabilityRatesContainer = view.findViewById(R.id.rates_container);

		emptyAvailabilitySummaryTextView.setVisibility(View.VISIBLE);
		ratesProgressBar.setVisibility(View.VISIBLE);
		emptyAvailabilitySummaryTextView.setText(context.getString(R.string.room_rates_loading));
		availabilityRatesContainer.setVisibility(View.GONE);
	}

	public static void showRatesContainer(View view) {

		// view is not created yet, so there's nothing to do here
		if (view == null) {
			return;
		}
		TextView emptyAvailabilitySummaryTextView = (TextView) view.findViewById(R.id.empty_summart_container);
		ProgressBar ratesProgressBar = (ProgressBar) view.findViewById(R.id.rates_progress_bar);
		View availabilityRatesContainer = view.findViewById(R.id.rates_container);

		emptyAvailabilitySummaryTextView.setVisibility(View.GONE);
		ratesProgressBar.setVisibility(View.GONE);
		availabilityRatesContainer.setVisibility(View.VISIBLE);
	}

	public static void showErrorForRates(View view, String string) {

		// view is not created yet, so there's nothing to do here
		if (view == null) {
			return;
		}

		TextView emptyAvailabilitySummaryTextView = (TextView) view.findViewById(R.id.empty_summart_container);
		ProgressBar ratesProgressBar = (ProgressBar) view.findViewById(R.id.rates_progress_bar);
		View availabilityRatesContainer = view.findViewById(R.id.rates_container);

		emptyAvailabilitySummaryTextView.setVisibility(View.VISIBLE);
		ratesProgressBar.setVisibility(View.INVISIBLE);
		emptyAvailabilitySummaryTextView.setText(string);
		availabilityRatesContainer.setVisibility(View.GONE);
	}

	private static void setHeightOfWeightOneForRow(View view) {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0);
		lp.weight = 1;
		view.setLayoutParams(lp);
	}

}
