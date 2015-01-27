package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarOffer;
import com.expedia.bookings.utils.DateFormatUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarCheckoutWidget extends LinearLayout {

	CarOffer offer;

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

	@InjectView(R.id.price_text)
	TextView tripTotalText;

	@InjectView(R.id.purchase_total_text_view)
	TextView sliderTotalText;


	public CarCheckoutWidget(Context context) {
		super(context);
	}

	public CarCheckoutWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	public void setOffer(CarOffer offer) {
		this.offer = offer;
		bind(offer);
	}

	private void bind(CarOffer offer) {
		categoryTitleText.setText(offer.vehicleInfo.category + " " + offer.vehicleInfo.type);
		carModelText.setText(offer.vehicleInfo.makes.get(0));
		airportText.setText(offer.pickUpLocation.locationDescription);
		dateTimeText.setText(DateFormatUtils
			.formatDateTimeRange(getContext(), CarDb.searchParams.startDateTime, CarDb.searchParams.endDateTime,
				DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT));
		tripTotalText.setText(offer.fare.total.getFormattedMoney());
		sliderTotalText.setText(offer.fare.total.getFormattedMoney());
	}
}
