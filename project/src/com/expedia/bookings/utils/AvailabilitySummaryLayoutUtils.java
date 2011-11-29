package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.BedType;
import com.expedia.bookings.data.Rate.BedTypeId;
import com.expedia.bookings.widget.SummarizedRoomRates;
import com.mobiata.android.text.StrikethroughTagHandler;

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
			layoutAvailabilitySummary(context, property, availabilityResponse, view, onRateClickListener);
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

	public static void setupAvailabilitySummary(final Context context, Property property, View view) {

		// view is not created yet, so there's nothing to do here
		if (view == null) {
			return;
		}

		View availabilitySummaryContainerCentered = view.findViewById(R.id.availability_summary_container);
		View availabilitySummaryContainerLeft = view.findViewById(R.id.availability_summary_container_left);
		View minPriceRow = view.findViewById(R.id.min_price_row_container);

		boolean isPropertyOnSale = property.getLowestRate().isOnSale();

		/*
		 * If the centered availability summary container does not exist,
		 * and the one that is left-aligned does, adjust the layout with that
		 * in mind (set the background of the right views)
		 */
		if (availabilitySummaryContainerCentered != null) {
			if (isPropertyOnSale) {
				availabilitySummaryContainerCentered.setBackgroundResource(R.drawable.bg_summarized_room_rates_sale);
			}
			else {
				availabilitySummaryContainerCentered.setBackgroundResource(R.drawable.bg_summarized_room_rates);
			}
		}
		else if (availabilitySummaryContainerLeft != null) {
			if (isPropertyOnSale) {
				minPriceRow.setBackgroundResource(R.drawable.bg_sale_ribbon_left);
			}
			else {
				minPriceRow.setBackgroundResource(R.drawable.bg_normal_ribbon_left);
			}
		}

		TextView basePrice = (TextView) minPriceRow.findViewById(R.id.base_price_text_view);
		TextView minPrice = (TextView) minPriceRow.findViewById(R.id.min_price_text_view);

		String displayRateString = StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate());

		Resources r = context.getResources();
		// style the minimum available price text
		StyleSpan textStyleSpan = new StyleSpan(Typeface.BOLD);
		ForegroundColorSpan textColorSpan = new ForegroundColorSpan(r.getColor(R.color.hotel_price_text_color));

		TextView perNighTextView = (TextView) minPriceRow.findViewById(R.id.per_night_text_view);
		perNighTextView.setTextColor(isPropertyOnSale ? r.getColor(android.R.color.white) : r
				.getColor(android.R.color.black));

		if (Rate.showInclusivePrices()) {
			perNighTextView.setVisibility(View.GONE);
		}
		else {
			perNighTextView.setVisibility(View.VISIBLE);
		}

		if (isPropertyOnSale) {
			basePrice.setVisibility(View.VISIBLE);
			String basePriceString = StrUtils.formatHotelPrice(property.getLowestRate().getDisplayBaseRate());

			Spanned basePriceStringSpanned = Html.fromHtml(r.getString(R.string.from_template, basePriceString), null,
					new StrikethroughTagHandler());
			basePrice.setText(basePriceStringSpanned);

			String textToMeasure = basePriceStringSpanned.toString() + displayRateString
					+ context.getString(R.string.per_night);

			if (tooLongToFitOnOneLine(availabilitySummaryContainerCentered, availabilitySummaryContainerLeft,
					textToMeasure, minPrice.getPaint())) {
				layoutBaseAndMinPriceOnTwoLines(context, basePrice, minPrice);
			}
			else {
				layoutBaseAndMinPriceSideBySide(context, basePrice, minPrice);
			}

			SpannableString str = new SpannableString(displayRateString);
			str.setSpan(textStyleSpan, 0, displayRateString.length(), 0);

			int whiteColor = r.getColor(android.R.color.white);

			minPrice.setText(str);
			minPrice.setTextColor(whiteColor);
			// remove shadow layer
			minPrice.setShadowLayer(0f, 0f, 0f, 0);

			basePrice.setTextColor(whiteColor);
		}
		else {
			layoutBaseAndMinPriceSideBySide(context, basePrice, minPrice);
			basePrice.setVisibility(View.GONE);

			String minPriceString = r.getString(R.string.min_room_price_template, displayRateString);
			SpannableString str = new SpannableString(minPriceString);

			ForegroundColorSpan textBlackColorSpan = new ForegroundColorSpan(r.getColor(android.R.color.black));
			int startingIndexOfDisplayRate = minPriceString.indexOf(displayRateString);

			str.setSpan(textStyleSpan, 0, minPriceString.length(), 0);
			str.setSpan(textColorSpan, startingIndexOfDisplayRate,
					startingIndexOfDisplayRate + displayRateString.length(), 0);
			str.setSpan(textBlackColorSpan, 0, startingIndexOfDisplayRate - 1, 0);

			minPrice.setText(str);
			float textSize = context.getResources().getDimension(R.dimen.min_price_row_text_normal);
			basePrice.setTextSize(textSize);
			minPrice.setTextSize(textSize);

			/*
			 * NOTE: Unsure as to why the text shadow layer is not applied 
			 * to the text view when the view is hardware rendered 
			 */
			minPrice.setShadowLayer(0.1f, 0f, 1f, context.getResources().getColor(R.color.text_shadow_color));
			minPrice.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

			perNighTextView.setShadowLayer(0.1f, 0f, 1f, context.getResources().getColor(R.color.text_shadow_color));
			perNighTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

	}

	private static void layoutBaseAndMinPriceSideBySide(Context context, TextView basePrice, TextView minPrice) {
		((RelativeLayout.LayoutParams) basePrice.getLayoutParams()).addRule(RelativeLayout.ALIGN_BASELINE,
				R.id.min_price_text_view);
		((RelativeLayout.LayoutParams) minPrice.getLayoutParams()).addRule(RelativeLayout.BELOW, 0);
		((RelativeLayout.LayoutParams) minPrice.getLayoutParams()).addRule(RelativeLayout.RIGHT_OF,
				R.id.base_price_text_view);
		((RelativeLayout.LayoutParams) minPrice.getLayoutParams()).topMargin = 0;
		((RelativeLayout.LayoutParams) basePrice.getLayoutParams()).bottomMargin = 0;

		float textSize = context.getResources().getDimension(R.dimen.min_price_row_text_normal);
		basePrice.setTextSize(textSize);
		minPrice.setTextSize(textSize);
	}

	private static void layoutBaseAndMinPriceOnTwoLines(Context context, TextView basePrice, TextView minPrice) {
		((RelativeLayout.LayoutParams) basePrice.getLayoutParams()).addRule(RelativeLayout.ALIGN_BASELINE, 0);
		((RelativeLayout.LayoutParams) minPrice.getLayoutParams()).addRule(RelativeLayout.BELOW,
				R.id.base_price_text_view);
		((RelativeLayout.LayoutParams) minPrice.getLayoutParams()).addRule(RelativeLayout.RIGHT_OF, 0);
		((RelativeLayout.LayoutParams) minPrice.getLayoutParams()).topMargin = 0;
		float textSize = context.getResources().getDimension(R.dimen.min_price_row_text_small);
		basePrice.setTextSize(textSize);
		minPrice.setTextSize(textSize);
	}

	private static boolean tooLongToFitOnOneLine(View availabilitySummaryContainerCentered,
			View availabilitySummaryContainerLeft, String textToMeasure, Paint paintToMeasureWith) {
		float measuredTextWidth = paintToMeasureWith.measureText(textToMeasure);

		if (availabilitySummaryContainerCentered != null) {
			if ((measuredTextWidth / availabilitySummaryContainerCentered.getMeasuredWidth()) > 0.8) {
				return true;
			}
		}
		else if (availabilitySummaryContainerLeft != null) {
			if ((measuredTextWidth / availabilitySummaryContainerLeft.getMeasuredWidth()) > 0.8) {
				return true;
			}
		}
		return false;
	}

	public static final Comparator<Rate> RATE_COMPARATOR = new Comparator<Rate>() {
		@Override
		public int compare(Rate rate1, Rate rate2) {

			Money lowRate1 = rate1.getDisplayRate();
			Money lowRate2 = rate2.getDisplayRate();

			// Check that we have rates to compare first
			if (lowRate1 == null && lowRate2 == null) {
				return 0;
			}
			else if (lowRate1 == null) {
				return -1;
			}
			else if (lowRate2 == null) {
				return 1;
			}

			// Compare rates
			double amount1 = lowRate1.getAmount();
			double amount2 = lowRate2.getAmount();
			if (amount1 == amount2) {
				return 0;
			}
			else if (amount1 > amount2) {
				return 1;
			}
			else {
				return -1;
			}
		}
	};

	private static void layoutAvailabilitySummary(Context context, Property property, AvailabilityResponse response,
			View view, final OnRateClickListener onRateClickListener) {

		// view is not created yet, so there's nothing to do here
		if (view == null) {
			return;
		}

		ViewGroup availabilityRatesContainer = (ViewGroup) view.findViewById(R.id.rates_container);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		boolean isPropertyOnSale = property.getLowestRate().isOnSale();
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

		SummarizedRoomRates summarizedRoomRates = response.getSummarizedRoomRates();
		
		Rate[] sortedRates = response.getRates().toArray(new Rate[0]).clone();
		Arrays.sort(sortedRates, RATE_COMPARATOR);

		for (int i = 0; i < MAX_SUMMARIZED_RATE_RESULTS; i++) {
			View summaryRow = availabilityRatesContainer.getChildAt(i);
			ObjectAnimator animator = ObjectAnimator.ofFloat(summaryRow, "alpha", 0, 1);
			animator.setDuration(ANIMATION_SPEED);
			animator.start();

			Rate rate = null;
			boolean useSummarizedRoomRates = false;
			if (summarizedRoomRates.numSummarizedRates() > 0 && i < summarizedRoomRates.numSummarizedRates()) {
				rate = summarizedRoomRates.getRate(i);
				useSummarizedRoomRates = true;
			}
			else if (summarizedRoomRates.numSummarizedRates() == 0 && response.getRateCount() > 0
					&& i < response.getRateCount()) {
				rate = sortedRates[i];
			}
			else {
				continue;
			}

			final Rate clickedRate = rate;
			summaryRow.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					onRateClickListener.onRateClick(clickedRate);
				}
			});

			boolean centeredLayout = (view.findViewById(R.id.availability_summary_container) != null);
			boolean leftAlignedLayout = (view.findViewById(R.id.availability_summary_container_left) != null);

			View chevron = summaryRow.findViewById(R.id.availability_chevron_image_view);
			TextView summaryDescription = (TextView) summaryRow.findViewById(R.id.availability_description_text_view);
			TextView priceTextView = (TextView) summaryRow.findViewById(R.id.availability_summary_price_text_view);
			View perNightTexView = summaryRow.findViewById(R.id.per_night_text_view);
			

			// make row elements visible since there's a price to display
			chevron.setVisibility(View.VISIBLE);
			if (perNightTexView != null) {
				perNightTexView.setVisibility(View.VISIBLE);
			}

			// determine description of room to display
			String description = null;
			if (useSummarizedRoomRates) {
				Pair<BedTypeId, Rate> pair = summarizedRoomRates.getBedTypeToRatePair(i);
				for (BedType bedType : pair.second.getBedTypes()) {
					if (bedType.bedTypeId == pair.first) {

						description = bedType.bedTypeDescription;
						break;
					}
				}
			}
			else {
				description = response.getRate(i).getRoomDescription();
			}

			// setup the row
			if (centeredLayout) {
				summaryDescription.setText(Html.fromHtml(context.getString(R.string.bed_type_start_value_template,
						description)));
			}
			else if (leftAlignedLayout) {
				SpannableString str = new SpannableString(description);
				StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
				str.setSpan(boldSpan, 0, description.length(), 0);
				summaryDescription.setText(str);
			}

			priceTextView.setText(StrUtils.formatHotelPrice(rate.getDisplayRate()));
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
		ratesProgressBar.setVisibility(View.GONE);
		emptyAvailabilitySummaryTextView.setText(string);
		availabilityRatesContainer.setVisibility(View.GONE);
	}

	private static void setHeightOfWeightOneForRow(View view) {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0);
		lp.weight = 1;
		view.setLayoutParams(lp);
	}

}
