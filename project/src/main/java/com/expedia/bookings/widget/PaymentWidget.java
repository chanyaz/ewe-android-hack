package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.User;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.NumberMaskFormatter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PaymentWidget extends ExpandableCardView {

	public PaymentWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@InjectView(R.id.section_billing_info)
	SectionBillingInfo sectionBillingInfo;

	@InjectView(R.id.section_location_address)
	SectionLocation sectionLocation;

	@InjectView(R.id.edit_creditcard_number)
	NumberMaskEditText creditCardNumber;

	@InjectView(R.id.edit_name_on_card)
	EditText creditCardName;

	@InjectView(R.id.edit_address_postal_code)
	EditText creditCardPostalCode;

	@InjectView(R.id.payment_button)
	PaymentButton paymentButton;

	@InjectView(R.id.stored_card_container)
	LinearLayout storedCardContainer;

	@InjectView(R.id.stored_card_name)
	TextView storedCardName;

	@InjectView(R.id.stored_card_expiration)
	TextView storedCardExpiration;

	@InjectView(R.id.display_credit_card_brand_icon_tablet)
	ImageView storedCardImageView;

	@InjectView(R.id.remove_stored_card_button)
	ImageView removedStoredCardImageView;

	@InjectView(R.id.paymentStatusIcon)
	CarDriverCheckoutStatusLeftImageView paymentStatusIcon;

	private boolean isCreditCardRequired = false;

	public void setCreditCardRequired(boolean required) {
		isCreditCardRequired = required;
		if (required) {
			setVisibility(VISIBLE);
		}
		else {
			setVisibility(GONE);
		}
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

	@OnClick(R.id.payment_info_card_view)
	public void onCardExpanded() {
		if (!isExpanded()) {
			setExpanded(true);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
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
		sectionBillingInfo.setLineOfBusiness(LineOfBusiness.CARS);
		sectionLocation.setLineOfBusiness(LineOfBusiness.CARS);
	}

	public void bind() {
		// User is logged in and has a stored card
		if (Db.getWorkingBillingInfoManager().getWorkingBillingInfo().hasStoredCard()) {
			StoredCreditCard card = Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getStoredCard();
			String cardName = card.getDescription();
			CreditCardType cardType = card.getType();
			bindCard(cardType, cardName, "");
			storedCardExpiration.setVisibility(GONE);
			paymentStatusIcon.setStatus(CarDriverWidget.DriverCheckoutStatus.COMPLETE);
			// let's bind sectionBillingInfo & sectionLocation to a new one. So next time around we start afresh.
			sectionBillingInfo.bind(new BillingInfo());
			sectionLocation.bind(new Location());
		}
		// Card info user entered is valid
		else if (sectionBillingInfo.performValidation() && sectionLocation.performValidation()) {
			BillingInfo info = sectionBillingInfo.getBillingInfo();
			String cardNumber = NumberMaskFormatter.obscureCreditCardNumber(info.getNumber());
			CreditCardType cardType = info.getCardType();
			String expiration = JodaUtils.format(info.getExpirationDate(), "MM/yy");
			bindCard(cardType, cardNumber, expiration);
			storedCardExpiration.setVisibility(VISIBLE);
			paymentStatusIcon.setStatus(CarDriverWidget.DriverCheckoutStatus.COMPLETE);
		}
		// Card info partially entered & not valid
		else if (isFilled() && (!sectionBillingInfo.performValidation() || !sectionLocation.performValidation())) {
			storedCardName.setText(getResources().getString(R.string.enter_payment_details));
			storedCardExpiration.setText("");
			storedCardExpiration.setVisibility(GONE);
			storedCardImageView.setImageResource(R.drawable.cars_checkout_cc_default_icon);
			paymentStatusIcon.setStatus(CarDriverWidget.DriverCheckoutStatus.INCOMPLETE);
		}
		// Default all fields are empty
		else {
			storedCardName.setText(getResources().getString(R.string.enter_payment_details));
			storedCardExpiration.setText("");
			storedCardExpiration.setVisibility(GONE);
			storedCardImageView.setImageResource(R.drawable.cars_checkout_cc_default_icon);
			paymentStatusIcon.setStatus(CarDriverWidget.DriverCheckoutStatus.DEFAULT);
			sectionBillingInfo.bind(new BillingInfo());
			sectionLocation.bind(new Location());
		}
	}

	private void bindCard(CreditCardType cardType, String cardNumber, String cardExpiration) {
		storedCardName.setText(cardNumber);
		storedCardExpiration.setText(getResources().getString(R.string.selected_card_template, cardExpiration));
		if (cardType != null) {
			storedCardImageView.setImageResource(BookingInfoUtils.getTabletCardIcon(cardType));
		}
		else {
			storedCardImageView.setImageResource(R.drawable.cars_checkout_cc_default_icon);
		}
	}

	@Override
	public void setExpanded(boolean expand, boolean animate) {
		super.setExpanded(expand, animate);
		if (expand && mToolbarListener != null) {
			mToolbarListener.setActionBarTitle(getActionBarTitle());
		}
		if (expand) {
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
				}
			}
			else {
				storedCardContainer.setVisibility(GONE);
				sectionBillingInfo.setVisibility(VISIBLE);
			}

			paymentStatusIcon.setVisibility(GONE);
			removedStoredCardImageView.setVisibility(VISIBLE);
			creditCardNumber.requestFocus();
		}
		else {
			paymentButton.setVisibility(GONE);
			storedCardContainer.setVisibility(VISIBLE);
			paymentStatusIcon.setVisibility(VISIBLE);
			removedStoredCardImageView.setVisibility(GONE);
			sectionBillingInfo.setVisibility(GONE);
		}
		bind();
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
		if (isComplete()) {
			setExpanded(false);
		}
	}

	@Override
	public void onLogin() {

	}

	@Override
	public void onLogout() {
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
		else {
			return (isCreditCardRequired && (Db.getWorkingBillingInfoManager().getWorkingBillingInfo().hasStoredCard() || (sectionBillingInfo.performValidation() && sectionLocation.performValidation())));
		}
	}

}
