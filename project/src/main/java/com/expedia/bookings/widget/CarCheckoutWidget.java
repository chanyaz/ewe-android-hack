package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.SettingUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;

public class CarCheckoutWidget extends CheckoutBasePresenter implements CVVEntryWidget.CVVEntryFragmentListener {

	public CarCheckoutWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	CreateTripCarOffer carProduct;
	String tripId;

	CarCheckoutSummaryWidget summaryWidget;

	protected LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.CARS;
	}

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
		String ogPriceForPriceChange = event.createTripResponse.searchCarOffer == null ?
			"" : event.createTripResponse.searchCarOffer.fare.total.formattedPrice;
		bind(event.createTripResponse.carProduct, ogPriceForPriceChange, event.createTripResponse.tripId);
		OmnitureTracking.trackAppCarCheckoutPage(getContext(), event.createTripResponse.carProduct);
	}

	@Subscribe
	public void onSignOut(Events.SignOut event) {
		loginWidget.accountLogoutClicked();
	}

	@Subscribe
	public void onShowCheckoutAfterPriceChange(Events.CarsShowCheckoutAfterPriceChange event) {
		bind(event.newCreateTripOffer, /* createTripOffer */
			event.originalCreateTripOffer.detailedFare.grandTotal.formattedPrice, /* originalPriceString */
			event.tripId /* tripId */);
		slideWidget.resetSlider();
	}

	private void bind(CreateTripCarOffer createTripOffer, String originalOfferFormattedPrice, String tripId) {
		this.tripId = tripId;
		this.carProduct = createTripOffer;
		summaryWidget.bind(carProduct, originalOfferFormattedPrice);
		paymentInfoCardView.setCreditCardRequired(carProduct.checkoutRequiresCard);
		clearCCNumber();
		scrollCheckoutToTop();
		slideWidget.resetSlider();

		int sliderMessage = carProduct.checkoutRequiresCard ? R.string.your_card_will_be_charged_TEMPLATE
			: R.string.amount_due_today_TEMPLATE;
		sliderTotalText.setText(getResources()
			.getString(sliderMessage, carProduct.detailedFare.totalDueToday.formattedPrice));

		mainContactInfoCardView.setExpanded(false);
		paymentInfoCardView.setExpanded(false);
		slideToContainer.setVisibility(INVISIBLE);

		legalInformationText.setText(
			StrUtils.generateLegalClickableLink(getContext(), carProduct.rulesAndRestrictionsURL));
		isCheckoutComplete();
		loginWidget.updateView();
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
		if (carProduct.checkoutRequiresCard) {
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
		final boolean suppressFinalBooking =
			BuildConfig.DEBUG && SettingUtils.get(getContext(), R.string.preference_suppress_car_bookings, true);
		CarCheckoutParamsBuilder builder =
			new CarCheckoutParamsBuilder()
				.firstName(mainContactInfoCardView.firstName.getText().toString())
				.lastName(mainContactInfoCardView.lastName.getText().toString())
				.emailAddress(User.isLoggedIn(getContext()) ? Db.getUser().getPrimaryTraveler().getEmail()
					: mainContactInfoCardView.emailAddress.getText().toString())
				.grandTotal(carProduct.detailedFare.grandTotal)
				.phoneCountryCode(
					Integer.toString(mainContactInfoCardView.phoneSpinner.getSelectedTelephoneCountryCode()))
				.phoneNumber(mainContactInfoCardView.phoneNumber.getText().toString())
				.tripId(tripId)
				.suppressFinalBooking(suppressFinalBooking);

		if (carProduct.checkoutRequiresCard && Db.getBillingInfo().hasStoredCard()) {
			builder.storedCCID(Db.getBillingInfo().getStoredCard().getId()).cvv(cvv);
		}
		else if (carProduct.checkoutRequiresCard) {
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

