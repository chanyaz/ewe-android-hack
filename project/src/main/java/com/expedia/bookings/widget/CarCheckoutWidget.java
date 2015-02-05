package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.DateFormatUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarCheckoutWidget extends LinearLayout implements SlideToWidget.ISlideToListener {

	public CarCheckoutWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	CarCreateTripResponse createTripResponse;

	@InjectView(R.id.edit_first_name)
	EditText firstName;

	@InjectView(R.id.edit_last_name)
	EditText lastName;

	@InjectView(R.id.edit_email_address)
	EditText emailAddress;

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

	@InjectView(R.id.slide_to_purchase_widget)
	SlideToWidget slideWidget;

	@InjectView(R.id.payment_info)
	ViewGroup paymentInfoBlock;

	@InjectView(R.id.phone_country_code_spinner)
	TelephoneSpinner phoneSpinner;

	@InjectView(R.id.edit_phone_number)
	EditText phoneNumber;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		slideWidget.addSlideToListener(this);

		// TODO - encapsulate data fields better, so that this isn't here.
		TelephoneSpinnerAdapter adapter = (TelephoneSpinnerAdapter) phoneSpinner.getAdapter();
		String targetCountry = getContext().getString(PointOfSale.getPointOfSale()
			.getCountryNameResId());
		for (int i = 0; i < adapter.getCount(); i++) {
			if (targetCountry.equalsIgnoreCase(adapter.getCountryName(i))) {
				phoneSpinner.setSelection(i);
				break;
			}
		}
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
		bind(event.createTripResponse);
	}

	private void bind(CarCreateTripResponse createTrip) {
		createTripResponse = createTrip;
		CreateTripCarOffer offer = createTripResponse.carProduct;

		if (offer.checkoutRequiresCard) {
			paymentInfoBlock.setVisibility(View.VISIBLE);
		}
		else {
			paymentInfoBlock.setVisibility(View.INVISIBLE);
		}

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

	@Subscribe
	public void onShowConfirmation(Events.CarsShowConfirmation event) {
		slideWidget.resetSlider();
	}

	//  SlideToWidget.ISlideToListener

	@Override
	public void onSlideStart() {
	}

	@Override
	public void onSlideAllTheWay() {
		CarCheckoutParamsBuilder builder =
			new CarCheckoutParamsBuilder()
				.firstName(firstName.getText().toString())
				.lastName(lastName.getText().toString())
				.emailAddress(emailAddress.getText().toString())
				.grandTotal(createTripResponse.carProduct.fare.grandTotal)
				.phoneCountryCode(Integer.toString(phoneSpinner.getSelectedTelephoneCountryCode()))
				.phoneNumber(phoneNumber.getText().toString())
				.tripId(createTripResponse.tripId);
		Events.post(new Events.CarsKickOffCheckoutCall(builder));
	}

	@Override
	public void onSlideAbort() {
	}
}
