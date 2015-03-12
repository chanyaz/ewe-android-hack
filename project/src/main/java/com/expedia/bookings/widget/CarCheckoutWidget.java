package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;

public class CarCheckoutWidget extends CheckoutBasePresenter implements CVVEntryWidget.CVVEntryFragmentListener {

	public CarCheckoutWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	CarCreateTripResponse createTripResponse;

	CarCheckoutSummaryWidget summaryWidget;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		summaryWidget = Ui.inflate(R.layout.car_checkout_summary_widget, summaryContainer, false);
		summaryContainer.addView(summaryWidget);
		mainContactInfoCardView.setEnterDetailsText(getResources().getString(R.string.enter_driver_details));
		paymentInfoCardView.setLineOfBusiness(LineOfBusiness.CARS);
	}

	@Subscribe
	public void onShowCheckout(Events.CarsShowCheckout event) {
		bind(event.createTripResponse);
	}

	private void bind(CarCreateTripResponse createTrip) {
		createTripResponse = createTrip;
		CreateTripCarOffer offer = createTripResponse.carProduct;
		summaryWidget.bind(offer);
		paymentInfoCardView.setCreditCardRequired(offer.checkoutRequiresCard);

		slideWidget.resetSlider();
		sliderTotalText.setText(getResources()
			.getString(R.string.your_card_will_be_charged_TEMPLATE, offer.detailedFare.totalDueToday.formattedPrice));

		mainContactInfoCardView.setExpanded(false);
		paymentInfoCardView.setExpanded(false);
		slideToContainer.setVisibility(INVISIBLE);

		// TODO Make this cars specific
		legalInformationText.setText(PointOfSale.getPointOfSale().getStylizedHotelBookingStatement());
		isCheckoutComplete();
		show(new CheckoutDefault());
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
		if (createTripResponse.carProduct.checkoutRequiresCard) {
			BillingInfo billingInfo = Db.getBillingInfo();
			Events.post(new Events.ShowCVV(billingInfo));
			slideWidget.resetSlider();
		}
		else {
			onBook(null);
		}

	}

	@Override
	public void onSlideAbort() {
	}

	@Override
	public void onBook(String cvv) {

		CarCheckoutParamsBuilder builder =
			new CarCheckoutParamsBuilder()
				.firstName(mainContactInfoCardView.firstName.getText().toString())
				.lastName(mainContactInfoCardView.lastName.getText().toString())
				.emailAddress(User.isLoggedIn(getContext()) ? Db.getUser().getPrimaryTraveler().getEmail()
					: mainContactInfoCardView.emailAddress.getText().toString())
				.grandTotal(createTripResponse.carProduct.detailedFare.grandTotal)
				.phoneCountryCode(
					Integer.toString(mainContactInfoCardView.phoneSpinner.getSelectedTelephoneCountryCode()))
				.phoneNumber(mainContactInfoCardView.phoneNumber.getText().toString())
				.tripId(createTripResponse.tripId);

		if (createTripResponse.carProduct.checkoutRequiresCard && Db.getBillingInfo().hasStoredCard()) {
			builder.storedCCID(Db.getBillingInfo().getStoredCard().getId()).cvv(cvv);
		}
		else if (createTripResponse.carProduct.checkoutRequiresCard) {
			BillingInfo info = Db.getBillingInfo();
			String expirationYear = JodaUtils.format(info.getExpirationDate(), "yyyy");
			String expirationMonth = JodaUtils.format(info.getExpirationDate(), "MM");

			builder.ccNumber(info.getNumber()).expirationYear(expirationYear)
				.expirationMonth(expirationMonth).ccPostalCode(info.getLocation().getPostalCode())
				.ccName(info.getNameOnCard()).cvv(cvv);
		}
		Events.post(new Events.CarsKickOffCheckoutCall(builder));
	}
}

