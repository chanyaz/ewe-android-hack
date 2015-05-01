package com.expedia.bookings.widget;

import javax.inject.Inject;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.LXUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.SettingUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;

public class LXCheckoutWidget extends CheckoutBasePresenter implements CVVEntryWidget.CVVEntryFragmentListener {

	public LXCheckoutWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	private static final String RULES_RESTRICTIONS_URL_PATH = "Checkout/LXRulesAndRestrictions?tripid=";
	@Inject
	LXState lxState;

	LXCheckoutSummaryWidget summaryWidget;

	LXCreateTripResponse createTripResponse;

	protected LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.LX;
	}

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

		OmnitureTracking.trackAppLXCheckoutPayment(getContext(), lxState);

		bind(event.createTripResponse);
	}

	private void bind(LXCreateTripResponse createTripResponse) {
		this.createTripResponse = createTripResponse;
		summaryWidget.bind();
		paymentInfoCardView.setCreditCardRequired(true);
		clearCCNumber();
		slideWidget.resetSlider();

		String totalMoney = LXUtils.getTotalAmount(lxState.selectedTickets).getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL);
		sliderTotalText.setText(getResources().getString(R.string.your_card_will_be_charged_TEMPLATE, totalMoney));

		mainContactInfoCardView.setExpanded(false);
		paymentInfoCardView.setExpanded(false);
		slideToContainer.setVisibility(INVISIBLE);

		String rulesAndRestrictionsURL = getRulesRestrictionsUrl(createTripResponse.tripId);
		legalInformationText.setText(StrUtils.generateLegalClickableLink(getContext(), rulesAndRestrictionsURL));
		isCheckoutComplete();
		loginWidget.updateView();
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
		final boolean suppressFinalBooking =
			BuildConfig.DEBUG && SettingUtils.get(getContext(), R.string.preference_suppress_lx_bookings, true);

		LXCheckoutParams checkoutParams = new LXCheckoutParams()
			.firstName(mainContactInfoCardView.firstName.getText().toString())
			.lastName(mainContactInfoCardView.lastName.getText().toString())
			.email(User.isLoggedIn(getContext()) ? Db.getUser().getPrimaryTraveler().getEmail()
				: mainContactInfoCardView.emailAddress.getText().toString())
			.expectedTotalFare(LXUtils.getTotalAmount(lxState.selectedTickets).getAmount().setScale(2).toString())
			.phoneCountryCode(
				Integer.toString(mainContactInfoCardView.phoneSpinner.getSelectedTelephoneCountryCode()))
			.phone(mainContactInfoCardView.phoneNumber.getText().toString())
			.expectedFareCurrencyCode(lxState.activity.price.currencyCode)
			.tripId(createTripResponse.tripId)
			.suppressFinalBooking(suppressFinalBooking);

		if (Db.getBillingInfo().hasStoredCard()) {
			checkoutParams.storedCreditCardId(Db.getBillingInfo().getStoredCard().getId()).cvv(cvv);
		}
		else {
			BillingInfo info = Db.getBillingInfo();
			String expirationYear = JodaUtils.format(info.getExpirationDate(), "yyyy");
			String expirationMonth = JodaUtils.format(info.getExpirationDate(), "MM");

			checkoutParams.creditCardNumber(info.getNumber())
				.expirationDateYear(expirationYear)
				.expirationDateMonth(expirationMonth)
				.postalCode(info.getLocation().getPostalCode())
				.nameOnCard(info.getNameOnCard()).cvv(cvv);
		}
		Events.post(new Events.LXKickOffCheckoutCall(checkoutParams));
	}

	private String getRulesRestrictionsUrl(String tripId) {
		String endpoint = Ui.getApplication(getContext()).appComponent().endpointProvider().getE3EndpointUrl(true);
		return endpoint + RULES_RESTRICTIONS_URL_PATH + tripId;
	}
}
