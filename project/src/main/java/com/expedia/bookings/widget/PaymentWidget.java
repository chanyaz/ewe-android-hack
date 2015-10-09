package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.TripBucketItem;
import com.expedia.bookings.data.TripBucketItemCar;
import com.expedia.bookings.data.User;
import com.expedia.bookings.section.ISectionEditable;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.CreditCardUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.NumberMaskFormatter;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PaymentWidget extends ExpandableCardView {

	private boolean isZipValidationRequired;

	public PaymentWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@InjectView(R.id.card_info_container)
	ViewGroup cardInfoContainer;

	@InjectView(R.id.section_billing_info_container)
	ViewGroup billingInfoContainer;

	@InjectView(R.id.section_billing_info)
	public SectionBillingInfo sectionBillingInfo;

	@InjectView(R.id.section_location_address)
	SectionLocation sectionLocation;

	@InjectView(R.id.edit_creditcard_number)
	NumberMaskEditText creditCardNumber;

	@InjectView(R.id.edit_name_on_card)
	EditText creditCardName;

	@InjectView(R.id.edit_address_postal_code)
	EditText creditCardPostalCode;

	@InjectView(R.id.card_info_icon)
	RoundImageView cardInfoIcon;

	@InjectView(R.id.card_info_name)
	TextView cardInfoName;

	@InjectView(R.id.card_info_expiration)
	TextView cardInfoExpiration;

	@InjectView(R.id.card_info_status_icon)
	ContactDetailsCompletenessStatusImageView paymentStatusIcon;

	@InjectView(R.id.payment_button)
	PaymentButton paymentButton;

	@InjectView(R.id.stored_card_container)
	LinearLayout storedCardContainer;

	@InjectView(R.id.display_credit_card_brand_icon_tablet)
	RoundImageView storedCardImageView;

	@InjectView(R.id.stored_card_name)
	TextView storedCardName;

	@InjectView(R.id.invalid_payment_container)
	ViewGroup invalidPaymentContainer;

	@InjectView(R.id.invalid_payment_text)
	TextView invalidPaymentText;

	private boolean isCreditCardRequired = false;

	private LineOfBusiness lineOfBusiness;

	public void setCreditCardRequired(boolean required) {
		isCreditCardRequired = required;
		setVisibility(required ? VISIBLE : GONE);
	}

	public boolean isCreditCardRequired() {
		return isCreditCardRequired;
	}

	@OnClick(R.id.remove_stored_card_button)
	public void onStoredCardRemoved() {
		StoredCreditCard currentCC = Db.getBillingInfo().getStoredCard();
		if (currentCC != null) {
			BookingInfoUtils.resetPreviousCreditCardSelectState(getContext(), currentCC);
		}
		Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(null);
		Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
		storedCardContainer.setVisibility(GONE);
		sectionBillingInfo.setVisibility(VISIBLE);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.payment_widget, this);
		ButterKnife.inject(this);

		creditCardPostalCode.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					setExpanded(false);
					mToolbarListener.onWidgetClosed();
				}
				return false;
			}
		});
		creditCardNumber.setOnFocusChangeListener(this);
		creditCardName.setOnFocusChangeListener(this);
		creditCardPostalCode.setOnFocusChangeListener(this);
		paymentButton.setPaymentButtonListener(paymentButtonListener);
		sectionBillingInfo.addInvalidCharacterListener(new InvalidCharacterHelper.InvalidCharacterListener() {
			@Override
			public void onInvalidCharacterEntered(CharSequence text, InvalidCharacterHelper.Mode mode) {
				ActionBarActivity activity = (ActionBarActivity) getContext();
				InvalidCharacterHelper.showInvalidCharacterPopup(activity.getSupportFragmentManager(), mode);
			}
		});
		sectionBillingInfo.addChangeListener(mValidFormsOfPaymentListener);
	}

	public void setLineOfBusiness(LineOfBusiness lineOfBusiness) {
		this.lineOfBusiness = lineOfBusiness;
		sectionBillingInfo.setLineOfBusiness(lineOfBusiness);
		sectionLocation.setLineOfBusiness(lineOfBusiness);
		paymentButton.setLineOfBusiness(lineOfBusiness);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		super.onFocusChange(v, hasFocus);
		if (hasFocus) {
			 if (v == creditCardPostalCode && isZipValidationRequired) {
				sectionLocation.resetValidation();
			 }
			sectionBillingInfo.resetValidation(v.getId(), true);
		}
	}

	public void bind() {
		// Should not perform validation unless the form has information in it
		boolean isFilled = isFilled();
		boolean isBillingInfoValid = isFilled && sectionBillingInfo.performValidation();
		boolean isPostalCodeValid = isFilled && sectionLocation.performValidation();
		// User is logged in and has a stored card
		if (Db.getWorkingBillingInfoManager().getWorkingBillingInfo().hasStoredCard()) {
			StoredCreditCard card = Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getStoredCard();
			String cardName = card.getDescription();
			CreditCardType cardType = card.getType();
			bindCard(cardType, cardName, null);
			paymentStatusIcon.setStatus(ContactDetailsCompletenessStatus.COMPLETE);
			// let's bind sectionBillingInfo & sectionLocation to a new one. So next time around we start afresh.
			reset();
		}
		// Card info user entered is valid
		else if (isBillingInfoValid && isPostalCodeValid) {
			BillingInfo info = sectionBillingInfo.getBillingInfo();
			String cardNumber = NumberMaskFormatter.obscureCreditCardNumber(info.getNumber());
			CreditCardType cardType = info.getCardType();
			String expiration = JodaUtils.format(info.getExpirationDate(), "MM/yy");
			bindCard(cardType, cardNumber, expiration);
			paymentStatusIcon.setStatus(ContactDetailsCompletenessStatus.COMPLETE);
			Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(info);
		}
		// Card info partially entered & not valid
		else if (isFilled() && (!isBillingInfoValid || !isPostalCodeValid)) {
			bindCard(null, getResources().getString(R.string.enter_payment_details), "");
			paymentStatusIcon.setStatus(ContactDetailsCompletenessStatus.INCOMPLETE);
		}
		// Default all fields are empty
		else {
			bindCard(null, getResources().getString(R.string.enter_payment_details), "");
			paymentStatusIcon.setStatus(ContactDetailsCompletenessStatus.DEFAULT);
			reset();
		}
	}

	private void reset() {
		sectionBillingInfo.bind(new BillingInfo());
		Location location = new Location();
		sectionBillingInfo.getBillingInfo().setLocation(location);
		sectionLocation.bind(location);
		sectionBillingInfo.resetValidation();
		sectionLocation.resetValidation();
	}

	private void bindCard(CreditCardType cardType, String cardNumber, String cardExpiration) {
		cardInfoName.setText(cardNumber);
		storedCardName.setText(cardNumber);
		if (!TextUtils.isEmpty(cardExpiration)) {
			cardInfoExpiration.setText(getResources().getString(R.string.selected_card_template, cardExpiration));
			cardInfoExpiration.setVisibility(VISIBLE);
		}
		else {
			cardInfoExpiration.setText("");
			cardInfoExpiration.setVisibility(GONE);
		}
		if (cardType != null) {
			cardInfoIcon.setImageDrawable(getContext().getResources().getDrawable(BookingInfoUtils.getTabletCardIcon(cardType)));
			storedCardImageView.setImageDrawable(getContext().getResources().getDrawable(BookingInfoUtils.getTabletCardIcon(cardType)));
		}
		else {
			cardInfoIcon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cars_checkout_cc_default_icon));
			storedCardImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cars_checkout_cc_default_icon));
		}
	}

	@Override
	public void setExpanded(boolean expand, boolean animate) {
		if (!isCreditCardRequired) {
			return;
		}
		super.setExpanded(expand, animate);
		if (expand && mToolbarListener != null) {
			mToolbarListener.setActionBarTitle(getActionBarTitle());
		}
		if (expand) {
			cardInfoContainer.setVisibility(GONE);
			billingInfoContainer.setVisibility(VISIBLE);
			if (User.isLoggedIn(getContext())) {
				paymentButton.setVisibility(VISIBLE);
				if (Db.getWorkingBillingInfoManager().getWorkingBillingInfo().hasStoredCard()) {
					storedCardContainer.setVisibility(VISIBLE);
					sectionBillingInfo.setVisibility(GONE);
					mToolbarListener.onEditingComplete();
				}
				else {
					storedCardContainer.setVisibility(GONE);
					sectionBillingInfo.setVisibility(VISIBLE);
					creditCardNumber.requestFocus();
					Ui.showKeyboard(creditCardNumber, null);
				}
			}
			else {
				paymentButton.setVisibility(GONE);
				storedCardContainer.setVisibility(GONE);
				sectionBillingInfo.setVisibility(VISIBLE);
				creditCardNumber.requestFocus();
				Ui.showKeyboard(creditCardNumber, null);
			}
			bind();
			paymentButton.bind();
			mValidFormsOfPaymentListener.onChange();
			OmnitureTracking.trackCheckoutPayment(lineOfBusiness);
		}
		else {
			cardInfoContainer.setVisibility(VISIBLE);
			billingInfoContainer.setVisibility(GONE);
			bind();
			Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
			paymentButton.dismissPopup();
		}
	}

	@Override
	public boolean getDoneButtonFocus() {
		if (creditCardName != null) {
			return creditCardName.hasFocus();
		}
		return false;
	}

	@Override
	public String getActionBarTitle() {
		return getResources().getString(R.string.cars_payment_details_text);
	}

	@Override
	public void onDonePressed() {
		boolean hasStoredCard = Db.getWorkingBillingInfoManager().getWorkingBillingInfo().hasStoredCard();
		boolean billingIsValid = !hasStoredCard && sectionBillingInfo.performValidation();
		boolean postalIsValid = !hasStoredCard && sectionLocation.performValidation();
		if (hasStoredCard || (billingIsValid && postalIsValid)) {
			setExpanded(false);
		}
	}

	@Override
	public void onLogin() {

	}

	@Override
	public void onLogout() {
		reset();
		setExpanded(false);
	}

	private PaymentButton.IPaymentButtonListener paymentButtonListener = new PaymentButton.IPaymentButtonListener() {
		@Override
		public void onAddNewCreditCardSelected() {

		}

		@Override
		public void onStoredCreditCardChosen() {
			storedCardContainer.setVisibility(VISIBLE);
			sectionBillingInfo.setVisibility(GONE);
			setExpanded(false);
		}
	};

	public boolean isFilled() {
		return !creditCardNumber.getText().toString().isEmpty() || !creditCardPostalCode.getText().toString().isEmpty() || !creditCardName.getText().toString().isEmpty() ;
	}

	@Override
	public boolean isComplete() {
		// Payment not required for this car booking.
		if (!isCreditCardRequired) {
			return true;
		}
		// If payment is required check to see if the entered/selected stored CC is valid.
		else if (isCreditCardRequired && (Db.getWorkingBillingInfoManager().getWorkingBillingInfo().hasStoredCard())) {
			return true;
		}
		else if (isCreditCardRequired && (isFilled() && sectionBillingInfo.performValidation() && sectionLocation.performValidation())) {
			return true;
		}

		return false;
	}

	public CreditCardType getCardType() {
		if (isCreditCardRequired && (Db.getWorkingBillingInfoManager().getWorkingBillingInfo().hasStoredCard())) {
			return Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getStoredCard().getType();
		}
		else if (isCreditCardRequired && (isFilled() && sectionBillingInfo.performValidation() && sectionLocation
			.performValidation())) {
			return sectionBillingInfo.getBillingInfo().getCardType();
		}

		return CreditCardType.UNKNOWN;
	}

	final ISectionEditable.SectionChangeListener mValidFormsOfPaymentListener = new ISectionEditable.SectionChangeListener() {
		@Override
		public void onChange() {
			if (sectionBillingInfo == null || sectionBillingInfo.getBillingInfo() == null) {
				return;
			}
			CreditCardType cardType = sectionBillingInfo.getBillingInfo().getCardType();
			TripBucketItem tripItem = Db.getTripBucket().getItem(lineOfBusiness);
			if (cardType != null && tripItem != null) {
				if (!tripItem.isCardTypeSupported(cardType)) {
					String cardName = CreditCardUtils.getHumanReadableName(getContext(), cardType);
					String message = null;
					if (lineOfBusiness.equals(LineOfBusiness.CARS)) {
						message = getResources().getString(R.string.car_does_not_accept_cardtype_TEMPLATE,
							((TripBucketItemCar)tripItem).mCarTripResponse.carProduct.vendor.name, cardName);
					}
					else if (lineOfBusiness.equals(LineOfBusiness.LX)) {
						message = getResources().getString(R.string.lx_does_not_accept_cardtype_TEMPLATE, cardName);
					}
					else if (lineOfBusiness.equals(LineOfBusiness.HOTELSV2)) {
						message = getResources().getString(R.string.hotel_does_not_accept_cardtype_TEMPLATE, cardName);
					}
					invalidPaymentText.setText(message);
					invalidPaymentContainer.setVisibility(VISIBLE);
				}
				else {
					invalidPaymentContainer.setVisibility(GONE);
				}
			}
			else {
				invalidPaymentContainer.setVisibility(GONE);
			}
		}
	};


	public void setZipValidationRequired(boolean zipValidationRequired) {
		this.isZipValidationRequired = zipValidationRequired;
	}
}
