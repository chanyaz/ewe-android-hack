package com.expedia.bookings.widget;

import javax.inject.Inject;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.lx.LXCheckoutParamsBuilder;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;

public class LXCheckoutWidget extends CheckoutBasePresenter implements CVVEntryWidget.CVVEntryFragmentListener {

	public LXCheckoutWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@Inject
	LXState lxState;

	LXCheckoutSummaryWidget summaryWidget;

	LXCreateTripResponse createTripResponse;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);

		summaryWidget = Ui.inflate(R.layout.lx_checkout_summary_widget, summaryContainer, false);
		summaryContainer.addView(summaryWidget);
		mainContactInfoCardView.setEnterDetailsText(getResources().getString(R.string.lx_enter_contact_details));
		paymentInfoCardView.setLineOfBusiness(LineOfBusiness.LX);
	}

	@Subscribe
	public void onShowCheckout(Events.LXCreateTripSucceeded event) {
		bind(event.createTripResponse);
	}

	private void bind(LXCreateTripResponse createTripResponse) {
		this.createTripResponse = createTripResponse;
		summaryWidget.bind();
		paymentInfoCardView.setCreditCardRequired(true);
		slideWidget.resetSlider();

		String grandTotal = String.format(getResources().getString(R.string.lx_total_price_with_currency_TEMPLATE),
			lxState.offer.currencySymbol, lxState.offerSelected.amount);
		sliderTotalText.setText(getResources().getString(R.string.your_card_will_be_charged_TEMPLATE, grandTotal));

		mainContactInfoCardView.setExpanded(false);
		paymentInfoCardView.setExpanded(false);
		slideToContainer.setVisibility(INVISIBLE);
		// TODO Make this LX specific
		legalInformationText.setText(PointOfSale.getPointOfSale().getStylizedHotelBookingStatement());
		isCheckoutComplete();
		show(new CheckoutDefault());
	}

	@Override
	public void onSlideStart() {
	}

	@Override
	public void onSlideProgress(float pixels, float total) {
	}

	@Override
	public void onSlideAllTheWay() {
		BillingInfo billingInfo = Db.getBillingInfo();
		Events.post(new Events.ShowCVV(billingInfo));
		slideWidget.resetSlider();
	}

	@Override
	public void onSlideAbort() {
	}

	@Override
	public void onBook(String cvv) {
		LXCheckoutParamsBuilder checkoutParamsBuilder = new LXCheckoutParamsBuilder()
			.firstName(mainContactInfoCardView.firstName.getText().toString())
			.lastName(mainContactInfoCardView.lastName.getText().toString())
			.email(User.isLoggedIn(getContext()) ? Db.getUser().getPrimaryTraveler().getEmail()
				: mainContactInfoCardView.emailAddress.getText().toString())
			.expectedTotalFare(lxState.offerSelected.amount.toString())
			.phoneCountryCode(
				Integer.toString(mainContactInfoCardView.phoneSpinner.getSelectedTelephoneCountryCode()))
			.phone(mainContactInfoCardView.phoneNumber.getText().toString())
			.expectedFareCurrencyCode(lxState.activity.currencyCode)
			.tripId(createTripResponse.tripId);

		if (Db.getBillingInfo().hasStoredCard()) {
			checkoutParamsBuilder.storedCreditCardId(Db.getBillingInfo().getStoredCard().getId()).cvv(cvv);
		}
		else {
			BillingInfo info = Db.getBillingInfo();
			String expirationYear = JodaUtils.format(info.getExpirationDate(), "yyyy");
			String expirationMonth = JodaUtils.format(info.getExpirationDate(), "MM");

			checkoutParamsBuilder.creditCardNumber(info.getNumber())
				.expirationDateYear(expirationYear)
				.expirationDateMonth(expirationMonth)
				.postalCode(info.getLocation().getPostalCode())
				.nameOnCard(info.getNameOnCard()).cvv(cvv);
		}
		Events.post(new Events.LXKickOffCheckoutCall(checkoutParamsBuilder));
	}
}
