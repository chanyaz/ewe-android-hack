package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.RateBreakdownItem;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.CheckoutSummaryWidgetUtils;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarCheckoutSummaryWidget extends RelativeLayout {

	public CarCheckoutSummaryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.car_vendor_text)
	TextView carCompanyText;

	@InjectView(R.id.category_title_text)
	TextView categoryTitleText;

	@InjectView(R.id.car_model_text)
	TextView carModelText;

	@InjectView(R.id.location_description_text)
	TextView locationDescriptionText;

	@InjectView(R.id.airport_text)
	TextView airportText;

	@InjectView(R.id.date_time_text)
	TextView dateTimeText;

	@InjectView(R.id.ticked_info_text_1)
	TextView tickedInfoText1;

	@InjectView(R.id.ticked_info_text_2)
	TextView tickedInfoText2;

	@InjectView(R.id.ticked_info_text_3)
	TextView tickedInfoText3;

	@InjectView(R.id.due_at_text)
	TextView dueAtText;

	@InjectView(R.id.price_text)
	TextView tripTotalText;

	@InjectView(R.id.price_change_container)
	ViewGroup priceChangeContainer;

	@InjectView(R.id.price_change_text)
	TextView priceChangeText;

	@InjectView(R.id.divider_line)
	View dividerLine;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	CreateTripCarOffer offer;

	public void bind(CreateTripCarOffer createTripOffer, String originalFormattedPrice) {
		offer = createTripOffer;
		if (!Strings.isEmpty(offer.pickUpLocation.airportInstructions)) {
			locationDescriptionText.setVisibility(View.VISIBLE);
			locationDescriptionText.setText(offer.pickUpLocation.airportInstructions);
		}
		else {
			locationDescriptionText.setVisibility(View.GONE);
		}
		carCompanyText.setText(offer.vendor.name);
		categoryTitleText.setText(offer.vehicleInfo.carCategoryDisplayLabel);
		carModelText.setText(CarDataUtils.getMakeName(getContext(), offer.vehicleInfo.makes));
		airportText.setText(offer.pickUpLocation.locationDescription);
		String tripTotal = Money.getFormattedMoneyFromAmountAndCurrencyCode(offer.detailedFare.grandTotal.amount,
			offer.detailedFare.grandTotal.getCurrency());
		tripTotalText.setText(tripTotal);
		tripTotalText.setContentDescription(Phrase.from(getContext(), R.string.car_selection_cost_summary_cont_desc_TEMPLATE)
				.put("trip_total", tripTotal).format().toString());
		dateTimeText.setText(DateFormatUtils
			.formatStartEndDateTimeRange(getContext(), offer.getPickupTime(), offer.getDropOffTime(), false));

		// Price change
		final boolean hasPriceChange = Strings.isNotEmpty(originalFormattedPrice);
		priceChangeContainer.setVisibility(hasPriceChange ? View.VISIBLE : View.GONE);
		if (hasPriceChange) {
			priceChangeText.setText(getResources().getString(R.string.price_changed_from_TEMPLATE,
				originalFormattedPrice));
		}

		updateTickedInfoTextFields();
		updateDueAtLabel();
	}

	private void updateDueAtLabel() {
		boolean anyPriceDueToday = anyPriceDue(offer.detailedFare.priceBreakdownOfTotalDueToday);
		boolean anyPriceDueAtPickup = anyPriceDue(offer.detailedFare.priceBreakdownOfTotalDueAtPickup);

		if (anyPriceDueToday && !anyPriceDueAtPickup) {
			//Everything due today!
			dueAtText.setText(getResources().getString(R.string.car_cost_breakdown_due_today));
			dueAtText.setVisibility(VISIBLE);
		}
		else if (!anyPriceDueToday && anyPriceDueAtPickup) {
			//Everything due at pickup!
			dueAtText.setText(getResources().getString(R.string.car_cost_breakdown_total_due));
			dueAtText.setVisibility(VISIBLE);
		}
		else {
			dueAtText.setVisibility(GONE);
		}
	}

	private void updateTickedInfoTextFields() {
		List<String> tickedInfoTextStringValues = new ArrayList<>();
		//Ordering Preference - Free Cancellation, Insurance Included, Unlimited Mileage
		if (offer.hasFreeCancellation) {
			tickedInfoTextStringValues.add(getResources().getString(R.string.free_cancellation));
		}
		boolean isUserBucketedForCarInsuranceIncludedCheckout = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppCarInsuranceIncludedCKO);
		if (isUserBucketedForCarInsuranceIncludedCheckout && offer.isInsuranceIncluded) {
			tickedInfoTextStringValues.add(getResources().getString(R.string.insurance_included));
		}
		if (offer.hasUnlimitedMileage) {
			tickedInfoTextStringValues.add(getResources().getString(R.string.unlimited_mileage));
		}

		for (int iInfoTextIndex = 0; iInfoTextIndex < 3; iInfoTextIndex++) {
			TextView infoTextView = null;
			switch (iInfoTextIndex) {
			case 0:
				infoTextView = tickedInfoText1;
				break;
			case 1:
				infoTextView = tickedInfoText2;
				break;
			case 2:
				infoTextView = tickedInfoText3;
				break;
			}

			if (iInfoTextIndex < tickedInfoTextStringValues.size()) {
				infoTextView.setVisibility(VISIBLE);
				infoTextView.setText(tickedInfoTextStringValues.get(iInfoTextIndex));
			}
			else {
				infoTextView.setVisibility(GONE);
			}
		}
		dividerLine.setVisibility(tickedInfoTextStringValues.size() == 3 ? VISIBLE : GONE);
	}

	@OnClick(R.id.price_text)
	public void showCarCostBreakdown() {
		buildCarBreakdownDialog(getContext(), offer);
	}

	private static boolean anyPriceDue(List<RateBreakdownItem> rateBreakdownItems) {
		return (rateBreakdownItems != null && rateBreakdownItems.size() > 0);
	}

	private void buildCarBreakdownDialog(Context context, CreateTripCarOffer offer) {
		List<RateBreakdownItem> rateBreakdownDueAtPickup = offer.detailedFare.priceBreakdownOfTotalDueAtPickup;
		List<RateBreakdownItem> rateBreakdownDueToday = offer.detailedFare.priceBreakdownOfTotalDueToday;

		View view = LayoutInflater.from(context).inflate(R.layout.cost_summary_alert, null);
		LinearLayout ll = Ui.findView(view, R.id.parent);

		if (rateBreakdownDueAtPickup != null && rateBreakdownDueAtPickup.size() > 0) {
			for (RateBreakdownItem item : rateBreakdownDueAtPickup) {
				if (item.price == null) {
					ll.addView(CheckoutSummaryWidgetUtils.addRow(context,
						CarDataUtils.getFareBreakdownType(context, item.type),
						getContext().getString(R.string.included)));
				}
				else {
					ll.addView(CheckoutSummaryWidgetUtils.addRow(context,
						CarDataUtils.getFareBreakdownType(context, item.type),
						Money.getFormattedMoneyFromAmountAndCurrencyCode(item.price.getAmount(),
							item.price.getCurrency())));
				}
			}
		}

		if (rateBreakdownDueToday != null && rateBreakdownDueToday.size() > 0) {
			for (RateBreakdownItem item : rateBreakdownDueToday) {
				if (item.price == null) {
					ll.addView(CheckoutSummaryWidgetUtils.addRow(context,
						CarDataUtils.getFareBreakdownType(context, item.type),
						getContext().getString(R.string.included)));
				}
				else {
					ll.addView(CheckoutSummaryWidgetUtils.addRow(context,
						CarDataUtils.getFareBreakdownType(context, item.type),
						Money.getFormattedMoneyFromAmountAndCurrencyCode(item.price.getAmount(),
							item.price.getCurrency())));
				}
			}
		}

		ll.addView(CheckoutSummaryWidgetUtils.addRow(context, context.getString(R.string.car_cost_breakdown_due_today),
			Money.getFormattedMoneyFromAmountAndCurrencyCode(offer.detailedFare.totalDueToday.getAmount(),
				offer.detailedFare.totalDueToday.getCurrency())));
		ll.addView(CheckoutSummaryWidgetUtils.addRow(context, context.getString(R.string.car_cost_breakdown_total_due),
			Money.getFormattedMoneyFromAmountAndCurrencyCode(offer.detailedFare.totalDueAtPickup.getAmount(),
				offer.detailedFare.totalDueAtPickup.getCurrency())));
		ll.addView(addDisclaimerRow(context, offer.pickUpLocation.countryCode,
			CarDataUtils.areTaxesAndFeesIncluded(rateBreakdownDueAtPickup)));

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(view);
		builder.setPositiveButton(context.getString(R.string.car_cost_breakdown_button_text),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					AccessibilityUtil.delayedFocusToView(tripTotalText, 300);
				}
			});
		builder.create().show();
	}

	private View addDisclaimerRow(Context context, String country, boolean areTaxesAndFeesDueAtPickupIncluded) {
		View row = LayoutInflater.from(context).inflate(R.layout.checkout_breakdown_price_disclaimer, null);
		TextView disclaimer = Ui.findView(row, R.id.price_disclaimer);
		String pos = PointOfSale.getPointOfSale().getThreeLetterCountryCode();
		boolean isCurrencySameAsPOS = Strings.equals(CurrencyUtils.currencyForLocale(country), CurrencyUtils.currencyForLocale(pos));
		//Do not say anything like "All taxes or fees due at pick-up..." if either the currency is same as POS or there are no Taxes/Fees Due At Pickup!
		disclaimer.setText((isCurrencySameAsPOS || areTaxesAndFeesDueAtPickupIncluded) ? context.getResources()
			.getString(R.string.cars_checkout_breakdown_us_text, CurrencyUtils.currencyForLocale(pos))
			: context.getResources().getString(R.string.cars_checkout_breakdown_non_us_text, CurrencyUtils.currencyForLocale(pos), CurrencyUtils.currencyForLocale(country)));
		return row;
	}
}
