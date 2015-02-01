package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.DateFormatUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;

public class CarCheckoutWidget extends LinearLayout {

	public CarCheckoutWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

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

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@Subscribe
	public void onShowCheckout(Events.CarsShowCheckout event) {
		CarDb.getCarServices().createTrip(event.offer.productKey, event.offer.fare.total.amount.toString(), new Observer<CarCreateTripResponse>() {
			@Override
			public void onCompleted() {
				// ignore
			}

			@Override
			public void onError(Throwable e) {
				throw new RuntimeException(e);
			}

			@Override
			public void onNext(CarCreateTripResponse response) {
				bind(response);
			}
		});
	}

	private void bind(CarCreateTripResponse createTrip) {
		CreateTripCarOffer offer = createTrip.carProduct;

		locationDescriptionText.setText(createTrip.itineraryNumber);

		categoryTitleText.setText(offer.vehicleInfo.category + " " + offer.vehicleInfo.type);
		carModelText.setText(offer.vehicleInfo.makes.get(0));
		airportText.setText(offer.pickUpLocation.locationDescription);
		tripTotalText.setText(offer.fare.grandTotal.getFormattedMoney());
		sliderTotalText.setText(offer.fare.grandTotal.getFormattedMoney());

		dateTimeText.setText(DateFormatUtils
			.formatDateTimeRange(getContext(), offer.pickupTime, offer.dropOffTime,
				DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT));
	}
}
