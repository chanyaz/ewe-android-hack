package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
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
		summaryWidget.bind(createTripResponse);
		paymentInfoCardView.setCreditCardRequired(createTripResponse.carProduct.checkoutRequiresCard);

		slideWidget.resetSlider();

		int sliderMessage = createTrip.carProduct.checkoutRequiresCard ? R.string.amount_due_today_TEMPLATE
			: R.string.your_card_will_be_charged_TEMPLATE;
		sliderTotalText.setText(getResources()
			.getString(sliderMessage, createTripResponse.carProduct.detailedFare.totalDueToday.formattedPrice));

		mainContactInfoCardView.setExpanded(false);
		paymentInfoCardView.setExpanded(false);
		slideToContainer.setVisibility(INVISIBLE);

		generateLegalClickableLink(legalInformationText);
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

	public void generateLegalClickableLink(TextView tv) {
		SpannableStringBuilder sb = new SpannableStringBuilder();

		String spannedRules = getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			createTripResponse.carProduct.rulesAndRestrictionsURL,
			getResources().getString(R.string.rules_and_restrictions));
		String spannedTerms = getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			PointOfSale.getPointOfSale().getTermsAndConditionsUrl(),
			getResources().getString(R.string.info_label_terms_conditions));
		String spannedPrivacy = getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			PointOfSale.getPointOfSale().getPrivacyPolicyUrl(), getResources().getString(R.string.privacy_policy));
		String statement = getResources()
			.getString(R.string.car_legal_TEMPLATE, spannedRules, spannedTerms, spannedPrivacy);

		sb.append(Html.fromHtml(statement));
		URLSpan[] spans = sb.getSpans(0, statement.length(), URLSpan.class);
		int start = 0;
		for (URLSpan o : spans) {
			start = sb.getSpanStart(o);
			break;
		}

		int end = sb.length();
		sb.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
		sb.setSpan(new UnderlineSpan(), start, end, 0);
		sb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.cars_primary_color)), start, end,
			Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		tv.setText(sb);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
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

