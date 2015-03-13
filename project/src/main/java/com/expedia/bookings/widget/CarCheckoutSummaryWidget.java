package com.expedia.bookings.widget;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.RateBreakdownItem;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.Ui;

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

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	CreateTripCarOffer offer;

	public void bind(CreateTripCarOffer offer) {
		this.offer = offer;
		locationDescriptionText.setText(offer.pickUpLocation.airportInstructions);
		carCompanyText.setText(offer.vendor.name);
		categoryTitleText.setText(offer.vehicleInfo.category + " " + offer.vehicleInfo.type);
		carModelText.setText(getContext().getString(R.string.car_model_name_template, offer.vehicleInfo.makes.get(0)));
		airportText.setText(offer.pickUpLocation.locationDescription);
		tripTotalText.setText(offer.detailedFare.grandTotal.formattedPrice);
		dateTimeText.setText(DateFormatUtils
			.formatDateTimeRange(getContext(), offer.pickupTime, offer.dropOffTime,
				DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT));
	}

	@OnClick(R.id.price_text)
	public void showCarCostBreakdown() {
		buildCarBreakdownDialog(getContext(), offer);
	}

	public static void buildCarBreakdownDialog(Context context, CreateTripCarOffer offer) {
		List<RateBreakdownItem> rateBreakdownDueAtPickup = offer.detailedFare.priceBreakdownOfTotalDueAtPickup;
		List<RateBreakdownItem> rateBreakdownDueToday = offer.detailedFare.priceBreakdownOfTotalDueToday;

		View view = LayoutInflater.from(context).inflate(R.layout.car_cost_summary_alert, null);
		LinearLayout ll = Ui.findView(view, R.id.parent);

		if (rateBreakdownDueAtPickup != null && rateBreakdownDueAtPickup.size() > 0) {
			for (RateBreakdownItem item : rateBreakdownDueAtPickup) {
				ll.addView(
					addRow(context, CarDataUtils.getFareBreakdownType(context, item.type), item.price.formattedPrice));
			}
		}

		if (rateBreakdownDueToday != null && rateBreakdownDueToday.size() > 0) {
			for (RateBreakdownItem item : rateBreakdownDueToday) {
				ll.addView(
					addRow(context, CarDataUtils.getFareBreakdownType(context, item.type), item.price.formattedPrice));
			}
		}
		ll.addView(addRow(context, context.getString(R.string.car_cost_breakdown_due_today),
			offer.detailedFare.totalDueToday.formattedPrice));
		ll.addView(addRow(context, context.getString(R.string.car_cost_breakdown_total_due),
			offer.detailedFare.totalDueAtPickup.formattedPrice));

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

	public static View addRow(Context context, String leftSideText, String rightSideText) {
		View row = LayoutInflater.from(context).inflate(R.layout.car_cost_summary_row, null);
		TextView priceDescription = Ui.findView(row, R.id.price_type_text_view);
		TextView priceValue = Ui.findView(row, R.id.price_text_view);
		priceDescription.setText(leftSideText);
		priceValue.setText(rightSideText);
		return row;
	}
}
