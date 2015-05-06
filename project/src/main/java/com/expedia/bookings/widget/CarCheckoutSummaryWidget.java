package com.expedia.bookings.widget;

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
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.RateBreakdownItem;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.CheckoutSummaryWidgetUtils;

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

	@InjectView(R.id.free_cancellation_text)
	TextView freeCancellationText;

	@InjectView(R.id.unlimited_mileage_text)
	TextView unlimitedMileageText;

	@InjectView(R.id.price_text)
	TextView tripTotalText;

	@InjectView(R.id.price_change_container)
	ViewGroup priceChangeContainer;

	@InjectView(R.id.price_change_text)
	TextView priceChangeText;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	CreateTripCarOffer offer;

	public void bind(CreateTripCarOffer createTripOffer, String originalFormattedPrice) {
		offer = createTripOffer;
		locationDescriptionText.setText(offer.pickUpLocation.airportInstructions);
		carCompanyText.setText(offer.vendor.name);
		categoryTitleText.setText(offer.vehicleInfo.carCategoryDisplayLabel);
		carModelText.setText(CarDataUtils.getMakeName(getContext(), offer.vehicleInfo.makes));
		airportText.setText(offer.pickUpLocation.locationDescription);
		tripTotalText.setText(offer.detailedFare.grandTotal.formattedPrice);
		dateTimeText.setText(DateFormatUtils
			.formatDateTimeRange(getContext(), offer.getPickupTime(), offer.getDropOffTime(),
				DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT));

		// Price change
		final boolean hasPriceChange = Strings.isNotEmpty(originalFormattedPrice);
		priceChangeContainer.setVisibility(hasPriceChange ? View.VISIBLE : View.GONE);
		if (hasPriceChange) {
			priceChangeText.setText(getResources().getString(R.string.price_changed_from_TEMPLATE,
				originalFormattedPrice));
		}
		freeCancellationText.setVisibility(offer.hasFreeCancellation ? VISIBLE : GONE);
		unlimitedMileageText.setVisibility(offer.hasUnlimitedMileage ? VISIBLE : GONE);
	}

	@OnClick(R.id.price_text)
	public void showCarCostBreakdown() {
		buildCarBreakdownDialog(getContext(), offer);
	}

	private void buildCarBreakdownDialog(Context context, CreateTripCarOffer offer) {
		List<RateBreakdownItem> rateBreakdownDueAtPickup = offer.detailedFare.priceBreakdownOfTotalDueAtPickup;
		List<RateBreakdownItem> rateBreakdownDueToday = offer.detailedFare.priceBreakdownOfTotalDueToday;

		View view = LayoutInflater.from(context).inflate(R.layout.cost_summary_alert, null);
		LinearLayout ll = Ui.findView(view, R.id.parent);

		if (rateBreakdownDueAtPickup != null && rateBreakdownDueAtPickup.size() > 0) {
			for (RateBreakdownItem item : rateBreakdownDueAtPickup) {
				ll.addView(CheckoutSummaryWidgetUtils.addRow(context,
					CarDataUtils.getFareBreakdownType(context, item.type),
					item.price.formattedPrice));
			}
		}

		if (rateBreakdownDueToday != null && rateBreakdownDueToday.size() > 0) {
			for (RateBreakdownItem item : rateBreakdownDueToday) {
				ll.addView(CheckoutSummaryWidgetUtils
					.addRow(context, CarDataUtils.getFareBreakdownType(context, item.type), item.price.formattedPrice));
			}
		}
		ll.addView(CheckoutSummaryWidgetUtils.addRow(context, context.getString(R.string.car_cost_breakdown_due_today),
			offer.detailedFare.totalDueToday.formattedPrice));
		ll.addView(CheckoutSummaryWidgetUtils.addRow(context, context.getString(R.string.car_cost_breakdown_total_due),
			offer.detailedFare.totalDueAtPickup.formattedPrice));
		ll.addView(addDisclaimerRow(context, offer.pickUpLocation.countryCode));

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(view);
		builder.setPositiveButton(context.getString(R.string.car_cost_breakdown_button_text),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		builder.create().show();
	}

	private View addDisclaimerRow(Context context, String country) {
		View row = LayoutInflater.from(context).inflate(R.layout.checkout_breakdown_price_disclaimer, null);
		TextView disclaimer = Ui.findView(row, R.id.price_disclaimer);
		String pos = PointOfSale.getPointOfSale().getThreeLetterCountryCode();
		boolean isCurrencySameAsPOS = Strings.equals(CurrencyUtils.currencyForLocale(country), CurrencyUtils.currencyForLocale(pos));
		disclaimer.setText(isCurrencySameAsPOS ? context.getResources()
			.getString(R.string.cars_checkout_breakdown_us_text, CurrencyUtils.currencyForLocale(pos))
			: context.getResources().getString(R.string.cars_checkout_breakdown_non_us_text, CurrencyUtils.currencyForLocale(pos), CurrencyUtils.currencyForLocale(country)));
		return row;
	}
}
