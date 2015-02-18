package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
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
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarCheckoutWidget extends FrameLayout implements SlideToWidgetJB.ISlideToListener {

	public CarCheckoutWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	CarCreateTripResponse createTripResponse;

	@InjectView(R.id.driver_info_container)
	android.widget.LinearLayout driverInfoContainer;

	@InjectView(R.id.checkout_scroll)
	ScrollView scrollView;

	@InjectView(R.id.edit_first_name)
	EditText firstName;

	@InjectView(R.id.edit_last_name)
	EditText lastName;

	@InjectView(R.id.edit_email_address)
	EditText emailAddress;

	@InjectView(R.id.car_vendor_text)
	TextView carCompanyText;

	@InjectView(R.id.legal_information_text_view)
	TextView legalInformationText;

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

	@InjectView(R.id.purchase_total_text_view)
	TextView sliderTotalText;

	@InjectView(R.id.payment_info_text)
	TextView paymentInfoText;

	@InjectView(R.id.driver_info_text)
	TextView driverInfoText;

	@InjectView(R.id.slide_to_purchase_layout)
	ViewGroup slideToContainer;

	@InjectView(R.id.slide_to_purchase_widget)
	SlideToWidgetJB slideWidget;

	@InjectView(R.id.payment_info)
	ViewGroup paymentInfoBlock;

	@InjectView(R.id.phone_country_code_spinner)
	TelephoneSpinner phoneSpinner;

	@InjectView(R.id.edit_phone_number)
	EditText phoneNumber;

	@InjectView(R.id.driver_info_card_view)
	CardView driverInfoCardView;

	@InjectView(R.id.payment_info_card_view)
	CardView paymentInfoCardView;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		slideWidget.addSlideToListener(this);

		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setTitle("Checkout");
		toolbar.setTitleTextColor(Color.WHITE);
		toolbar.setBackgroundColor(getResources().getColor(R.color.cars_primary_color));
		toolbar.inflateMenu(R.menu.cars_checkout_menu);
		MenuItem item = toolbar.getMenu().findItem(R.id.menu_done);
		item.setVisible(true);

		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.menu_done:
					Ui.hideKeyboard(CarCheckoutWidget.this);
					menuItem.setVisible(false);
					slideToContainer.setVisibility(View.VISIBLE);
					break;
				}
				return false;
			}
		});

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
			paymentInfoBlock.setVisibility(View.GONE);
		}

		locationDescriptionText.setText(offer.pickUpLocation.airportInstructions);

		MenuItem item = toolbar.getMenu().findItem(R.id.menu_done);
		item.setVisible(true);

		carCompanyText.setText(offer.vendor.name);
		categoryTitleText.setText(offer.vehicleInfo.category + " " + offer.vehicleInfo.type);
		carModelText.setText(offer.vehicleInfo.makes.get(0));
		airportText.setText(offer.pickUpLocation.locationDescription);
		tripTotalText.setText(offer.fare.grandTotal.getFormattedMoney());
		sliderTotalText.setText(getResources().getString(R.string.your_card_will_be_charged_TEMPLATE, offer.fare.grandTotal.getFormattedMoney()));

		dateTimeText.setText(DateFormatUtils
			.formatDateTimeRange(getContext(), offer.pickupTime, offer.dropOffTime,
				DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT));

		Drawable drawableEnabled = getResources().getDrawable(R.drawable.ic_action_bar_checkmark_white);
		drawableEnabled.setColorFilter(getResources().getColor(R.color.cars_checkmark_color), PorterDuff.Mode.SRC_IN);
		freeCancellationText.setCompoundDrawablesWithIntrinsicBounds(drawableEnabled, null, null, null);
		unlimitedMileageText.setCompoundDrawablesWithIntrinsicBounds(drawableEnabled, null, null, null);
		driverInfoText.setVisibility(VISIBLE);
		driverInfoContainer.setVisibility(GONE);
		paymentInfoText.setVisibility(VISIBLE);
		paymentInfoBlock.setVisibility(GONE);
		slideToContainer.setVisibility(View.GONE);
		driverInfoCardView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				driverInfoText.setVisibility(GONE);
				driverInfoContainer.setVisibility(VISIBLE);
			}
		});
		paymentInfoCardView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				paymentInfoText.setVisibility(GONE);
				paymentInfoBlock.setVisibility(VISIBLE);
			}
		});
		legalInformationText.setText(PointOfSale.getPointOfSale().getStylizedHotelBookingStatement());
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
	public void onSlideProgress(float pixels, float total) {
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
