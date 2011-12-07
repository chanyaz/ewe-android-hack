package com.expedia.bookings.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
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
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.BedType;
import com.expedia.bookings.data.Rate.BedTypeId;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.widget.SummarizedRoomRates;
import com.mobiata.android.util.AndroidUtils;

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
			if (availabilityResponse.hasErrors()) {
				showErrorForRates(view, availabilityResponse.getErrors().get(0).getPresentableMessage(context));
			}
			else {
				layoutAvailabilitySummary(context, property, availabilityResponse, view, onRateClickListener);
				emptyAvailabilitySummaryTextView.setVisibility(View.GONE);
				ratesProgressBar.setVisibility(View.GONE);
			}
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
		Resources r = context.getResources();

		boolean isPropertyOnSale = property.getLowestRate().isOnSale();
		String displayRateString = StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate());

		/*
		 * Styling
		 */
		StyleSpan textStyleSpan = new StyleSpan(Typeface.BOLD);
		ForegroundColorSpan textColorSpan = new ForegroundColorSpan(r.getColor(R.color.hotel_price_text_color));

		boolean useCondensedActionBar;
		if (AndroidUtils.getSdkVersion() >= 13) {
			useCondensedActionBar = r.getConfiguration().screenWidthDp <= 800;
		}
		else {
			useCondensedActionBar = r.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		}

		SpannableString basePriceSpannableString = null;
		SpannableString minPriceSpannableString = null;
		float textSize = 0.0f;
		boolean twoLineLayout = false;

		if (isPropertyOnSale) {
			String basePriceString = StrUtils.formatHotelPrice(property.getLowestRate().getDisplayBaseRate());
			String basePriceStringWithFrom = useCondensedActionBar ? basePriceString : r.getString(
					R.string.min_room_price_template, basePriceString);
			basePriceSpannableString = new SpannableString(basePriceStringWithFrom);

			int startingIndexOfBasePrice = basePriceStringWithFrom.indexOf(basePriceString);

			// 11364: ensuring to specifically handle both cases where the "from" word
			// can be before or after the base price
			if (startingIndexOfBasePrice > 0) {
				basePriceSpannableString.setSpan(textStyleSpan, 0, startingIndexOfBasePrice - 1, 0);
			}
			else if (startingIndexOfBasePrice == 0) {
				basePriceSpannableString.setSpan(textStyleSpan, startingIndexOfBasePrice + basePriceString.length(),
						basePriceStringWithFrom.length(), 0);
			}
			
			// strike through the baes price to indicate sale
			basePriceSpannableString.setSpan(new StrikethroughSpan(), startingIndexOfBasePrice,
					startingIndexOfBasePrice + basePriceString.length(), 0);
			
			// decrease the size of the base price so that the sale price is more prominent
			basePriceSpannableString.setSpan(new AbsoluteSizeSpan(16, true), startingIndexOfBasePrice,
					startingIndexOfBasePrice + basePriceString.length(), 0);

			minPriceSpannableString = new SpannableString(displayRateString);
			// bold the sale price
			minPriceSpannableString.setSpan(textStyleSpan, 0, displayRateString.length(), 0);

			String textToMeasure = basePriceStringWithFrom + displayRateString + context.getString(R.string.per_night);
			Paint paint = new Paint();
			paint.setTextSize(r.getDimension(R.dimen.min_price_row_text_normal));

			// if the base and minimum price are too long to fit on one line, use a two line approach instead
			if (tooLongToFitOnOneLine(availabilitySummaryContainerCentered, availabilitySummaryContainerLeft,
					textToMeasure, paint)) {
				twoLineLayout = true;
				textSize = context.getResources().getDimension(R.dimen.min_price_row_text_small);
			}
			else {
				textSize = context.getResources().getDimension(R.dimen.min_price_row_text_normal);
			}

		}
		else {
			String minPriceString = useCondensedActionBar ? displayRateString : r.getString(
					R.string.min_room_price_template, displayRateString);
			minPriceSpannableString = new SpannableString(minPriceString);

			ForegroundColorSpan textBlackColorSpan = new ForegroundColorSpan(r.getColor(android.R.color.black));
			int startingIndexOfDisplayRate = minPriceString.indexOf(displayRateString);

			// bold the starting price
			minPriceSpannableString.setSpan(textStyleSpan, 0, minPriceString.length(), 0);
			
			// set the starting price to be the color black
			minPriceSpannableString.setSpan(textColorSpan, startingIndexOfDisplayRate, startingIndexOfDisplayRate
					+ displayRateString.length(), 0);

			// 11364: ensuring to specifically handle the case where the "from" word can be before
			// or after the min price
			if (startingIndexOfDisplayRate > 0) {
				minPriceSpannableString.setSpan(textBlackColorSpan, 0, startingIndexOfDisplayRate - 1, 0);
			}
			else if (startingIndexOfDisplayRate == 0) {
				minPriceSpannableString.setSpan(textBlackColorSpan,
						startingIndexOfDisplayRate + displayRateString.length(), minPriceString.length(), 0);
			}
			
			textSize = context.getResources().getDimension(R.dimen.min_price_row_text_normal);
		}

		TextView basePriceOneLine = (TextView) minPriceRow.findViewById(R.id.base_price_text_view);
		TextView minPriceOneLine = (TextView) minPriceRow.findViewById(R.id.min_price_text_view);
		TextView perNightTextViewOneLine = (TextView) minPriceRow.findViewById(R.id.per_night_text_view);
		TextView basePriceFirstLine = (TextView) minPriceRow.findViewById(R.id.base_price_text_view_first_line);
		TextView minPriceOnSecondLine = (TextView) minPriceRow.findViewById(R.id.min_price_text_view_second_line);
		TextView perNightTextViewOnSecondLine = (TextView) minPriceRow
				.findViewById(R.id.per_night_text_view_second_line);

		TextView basePrice = null;
		TextView minPrice = null;
		TextView perNightTextView = null;

		if (twoLineLayout) {
			basePrice = basePriceFirstLine;
			minPrice = minPriceOnSecondLine;
			perNightTextView = perNightTextViewOnSecondLine;

			basePriceOneLine.setVisibility(View.GONE);
			minPriceOneLine.setVisibility(View.GONE);
			perNightTextViewOneLine.setVisibility(View.GONE);

			basePriceFirstLine.setVisibility(View.VISIBLE);
			minPriceOnSecondLine.setVisibility(View.VISIBLE);
			perNightTextViewOnSecondLine.setVisibility(View.VISIBLE);
		}
		else {
			basePrice = basePriceOneLine;
			minPrice = minPriceOneLine;
			perNightTextView = perNightTextViewOneLine;

			basePriceOneLine.setVisibility(View.VISIBLE);
			minPriceOneLine.setVisibility(View.VISIBLE);
			perNightTextViewOneLine.setVisibility(View.VISIBLE);

			basePriceFirstLine.setVisibility(View.GONE);
			minPriceOnSecondLine.setVisibility(View.GONE);
			perNightTextViewOnSecondLine.setVisibility(View.GONE);
		}

		if (basePriceSpannableString != null) {
			basePrice.setText(basePriceSpannableString);
			basePrice.setTextColor(Color.BLACK);
			basePrice.setShadowLayer(0.1f, 0f, 1f, r.getColor(R.color.text_shadow_color));
			basePrice.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			basePrice.setVisibility(View.VISIBLE);
			basePrice.setTextSize(textSize);
		}
		else {
			basePrice.setVisibility(View.GONE);
		}

		if (Rate.showInclusivePrices()) {
			perNightTextView.setVisibility(View.GONE);
		}
		else {
			perNightTextView.setVisibility(View.VISIBLE);
		}

		/*
		 * NOTE: Unsure as to why the text shadow layer is not applied 
		 * to the text view when the view is hardware rendered 
		 */
		minPrice.setText(minPriceSpannableString);
		minPrice.setShadowLayer(0.1f, 0f, 1f, r.getColor(R.color.text_shadow_color));
		minPrice.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		minPrice.setTextColor(r.getColor(R.color.hotel_price_text_color));
		minPrice.setTextSize(textSize);

		perNightTextView.setTextColor(Color.BLACK);
		perNightTextView.setShadowLayer(0.1f, 0f, 1f, r.getColor(R.color.text_shadow_color));
		perNightTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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

		boolean useSummarizedRates = summarizedRoomRates.numSummarizedRates() > 0;

		// determine the minimum rate to show
		Rate minimumRate = null;
		if (useSummarizedRates) {
			minimumRate = summarizedRoomRates.getStartingRate();
		}
		else if (response.getRateCount() > 0) {
			minimumRate = sortedRates[0];
		}

		int rateCount = sortedRates.length;
		int ratePickerPosition = 0;
		int summaryRowPosition = 0;

		// display rates in the summary container as long as one of the conditions continue to be met:
		// a) we are not past the maximum 3 rates to display 
		// b) there is atleast a minimum rate available
		// c) the rates in the summarized rates container have not been exhausted
		// d) if there are no summarized rates, the sorted rates have not been exhausted
		while (summaryRowPosition < MAX_SUMMARIZED_RATE_RESULTS
				&& ((summaryRowPosition == 0 && (summarizedRoomRates.getStartingRate() != null || rateCount > 0))
						|| (useSummarizedRates && ratePickerPosition < summarizedRoomRates.numSummarizedRates()) || (!useSummarizedRates && ratePickerPosition < rateCount - 1))) {

			View summaryRow = availabilityRatesContainer.getChildAt(summaryRowPosition);
			ObjectAnimator animator = ObjectAnimator.ofFloat(summaryRow, "alpha", 0, 1);
			animator.setDuration(ANIMATION_SPEED);
			animator.start();

			boolean showMinimumRate = (summaryRowPosition == 0);
			Rate rate = null;
			if (showMinimumRate) {
				rate = minimumRate;
			}
			else if (useSummarizedRates) {
				rate = summarizedRoomRates.getRate(ratePickerPosition);
			}
			else {
				rate = sortedRates[ratePickerPosition + 1];
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
			TextView perNightTexView = (TextView) summaryRow.findViewById(R.id.per_night_text_view);

			List<RateBreakdown> rateBreakdown = rate.getRateBreakdownList();
			int perNightTextId = R.string.per_night;
			boolean hidePerNightTextView = false;
			if (!Rate.showInclusivePrices()) {
				if (rateBreakdown == null) {
					// If rateBreakdown is null, we assume that this is a per/night hotel
					perNightTextId = R.string.rate_per_night;
				}
				else if (rateBreakdown.size() > 1) {
					if (rate.rateChanges()) {
						perNightTextId = R.string.rate_avg_per_night;
					}
					else {
						perNightTextId = R.string.rate_per_night;
					}
				}
			}
			else {
				hidePerNightTextView = true;
			}

			// make row elements visible since there's a price to display
			chevron.setVisibility(View.VISIBLE);

			// ensure to only show the per night section if necessary
			if (perNightTexView != null && !hidePerNightTextView) {
				perNightTexView.setVisibility(View.VISIBLE);
				perNightTexView.setText(context.getString(perNightTextId));
			}

			// determine description of room to display
			String description = null;
			if (showMinimumRate) {
				if (minimumRate.getBedTypes() != null) {
					description = minimumRate.getBedTypes().iterator().next().bedTypeDescription;
				}
				else {
					description = minimumRate.getRoomDescription();
				}
			}
			else if (useSummarizedRates) {
				Pair<BedTypeId, Rate> pair = summarizedRoomRates.getBedTypeToRatePair(ratePickerPosition);
				for (BedType bedType : rate.getBedTypes()) {
					if (bedType.bedTypeId == pair.first) {
						description = bedType.bedTypeDescription;
						break;
					}
				}
			}
			else {
				description = rate.getRoomDescription();
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

			// since we are using the minimum rate, don't increment
			// the position at which to pick a rate from
			if (!showMinimumRate) {
				ratePickerPosition++;
			}
			summaryRowPosition++;
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
