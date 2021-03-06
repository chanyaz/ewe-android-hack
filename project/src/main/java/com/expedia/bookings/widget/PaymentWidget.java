package com.expedia.bookings.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.GoogleWalletActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.TripBucketItem;
import com.expedia.bookings.data.TripBucketItemCar;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.section.ISectionEditable;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.tracking.HotelV2Tracking;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.CreditCardUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.NumberMaskFormatter;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.WalletUtils;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PaymentWidget extends ExpandableCardView {
	public static final int REQUEST_CODE_GOOGLE_WALLET_ACTIVITY = 1989;

	private boolean isZipValidationRequired;
	protected Presenter presenter;

	public PaymentWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@InjectView(R.id.card_info_container)
	ViewGroup cardInfoContainer;

	@InjectView(R.id.section_billing_info_container)
	ViewGroup billingInfoContainer;

	@InjectView(R.id.section_payment_options_container)
	ViewGroup paymentOptionsContainer;

	@InjectView(R.id.payment_option_credit_debit)
	TextView paymentOptionCreditDebitCard;

	@InjectView(R.id.payment_option_google_wallet)
	TextView paymentOptionGoogleWallet;

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

	protected LineOfBusiness lineOfBusiness;

	public void setCreditCardRequired(boolean required) {
		isCreditCardRequired = required;
		setVisibility(required ? VISIBLE : GONE);
	}

	public boolean isCreditCardRequired() {
		return isCreditCardRequired;
	}

	@OnClick(R.id.stored_card_container)
	public void onStoredCardClicked() {
		StoredCreditCard storedCard = Db.getBillingInfo().getStoredCard();
		if (storedCard != null && storedCard.isGoogleWallet()) {
			openGoogleWallet();
		}

	}

	protected void removeStoredCard() {
		StoredCreditCard currentCC = sectionBillingInfo.getBillingInfo().getStoredCard();
		BookingInfoUtils.resetPreviousCreditCardSelectState(getContext(), currentCC);
		Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(null);
		Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
		reset();
	}

	@OnClick(R.id.remove_stored_card_button)
	public void onStoredCardRemoved() {
		removeStoredCard();
		storedCardContainer.setVisibility(GONE);
		if (User.isLoggedIn(getContext())) {
			sectionBillingInfo.setVisibility(VISIBLE);
		}
		else {
			setExpanded(true, false);
		}
	}

	@OnClick(R.id.payment_option_credit_debit)
	public void creditCardClicked() {
		showPaymentDetails();
	}

	@OnClick(R.id.payment_option_google_wallet)
	public void googleWalletClicked() {
		openGoogleWallet();
	}

	@OnClick(R.id.select_payment_button)
	public void  onSelectSavedCardButtonClick() {
		paymentButton.showStoredCards();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		creditCardName.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					setExpanded(false);
					if (mToolbarListener != null) {
						mToolbarListener.onWidgetClosed();
					}
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
				AppCompatActivity activity = (AppCompatActivity) getContext();
				InvalidCharacterHelper.showInvalidCharacterPopup(activity.getSupportFragmentManager(), mode);
			}
		});
		sectionBillingInfo.addChangeListener(mValidFormsOfPaymentListener);

	}

	protected Drawable getCreditCardIcon() {
		Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_hotel_credit_card).mutate();
		icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.hotels_primary_color), PorterDuff.Mode.SRC_IN);
		return icon;
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
		boolean hasStoredCard = hasStoredCard();
		boolean isBillingInfoValid = isFilled && sectionBillingInfo.performValidation();
		boolean isPostalCodeValid = isFilled && sectionLocation.performValidation();
		// User is logged in and has a stored card
		if (!WalletUtils.isWalletSupported(lineOfBusiness) && sectionBillingInfo.getBillingInfo() != null && sectionBillingInfo.getBillingInfo().isUsingGoogleWallet()) {
			if (lineOfBusiness == LineOfBusiness.HOTELSV2) {
				paymentType(null, getResources().getString(R.string.checkout_hotelsv2_enter_payment_details_line1), "");
			}
			else {
				paymentType(null, getResources().getString(R.string.enter_payment_details), "");
			}
			paymentStatusIcon.setStatus(ContactDetailsCompletenessStatus.DEFAULT);
			reset();
		}
		else if (hasStoredCard) {
			StoredCreditCard card = sectionBillingInfo.getBillingInfo().getStoredCard();
			String cardName = card.getDescription();
			PaymentType paymentType = card.getType();
			if (card.isGoogleWallet()) {
				paymentType(paymentType, cardName,
					getResources().getString(R.string.checkout_payment_option_android_pay_label));
			}
			else {
				paymentType(paymentType, cardName, getResources().getString(R.string.checkout_payment_line2_storedcc));
			}
			paymentStatusIcon.setStatus(ContactDetailsCompletenessStatus.COMPLETE);
		}
		// Card info user entered is valid
		else if (isBillingInfoValid && isPostalCodeValid) {
			BillingInfo info = sectionBillingInfo.getBillingInfo();
			String cardNumber = NumberMaskFormatter.obscureCreditCardNumber(info.getNumber());
			PaymentType paymentType = info.getPaymentType();
			String expiration = JodaUtils.format(info.getExpirationDate(), "MM/yy");
			paymentType(paymentType, cardNumber, getResources().getString(R.string.selected_card_template, expiration));
			paymentStatusIcon.setStatus(ContactDetailsCompletenessStatus.COMPLETE);
			Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(info);
		}
		// Card info partially entered & not valid
		else if (isFilled() && (!isBillingInfoValid || !isPostalCodeValid)) {
			if (lineOfBusiness == LineOfBusiness.HOTELSV2) {
				paymentType(null, getResources().getString(R.string.checkout_hotelsv2_enter_payment_details_line1), "");
			}
			else {
				paymentType(null, getResources().getString(R.string.enter_payment_details), "");
			}
			paymentStatusIcon.setStatus(ContactDetailsCompletenessStatus.INCOMPLETE);
		}
		// Default all fields are empty
		else {
			if (lineOfBusiness == LineOfBusiness.HOTELSV2) {
				paymentType(null, getResources().getString(R.string.checkout_hotelsv2_enter_payment_details_line1), "");
			}
			else {
				paymentType(null, getResources().getString(R.string.enter_payment_details), "");
			}
			paymentStatusIcon.setStatus(ContactDetailsCompletenessStatus.DEFAULT);
			reset();
		}
	}

	protected void reset() {
		sectionBillingInfo.bind(new BillingInfo());
		Location location = new Location();
		sectionBillingInfo.getBillingInfo().setLocation(location);
		sectionLocation.bind(location);
		sectionBillingInfo.resetValidation();
		sectionLocation.resetValidation();
	}


	public void selectFirstAvailableCard() {
		Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(new BillingInfo());
		StoredCreditCard currentCC = Db.getBillingInfo().getStoredCard();
		if (currentCC != null) {
			BookingInfoUtils.resetPreviousCreditCardSelectState(getContext(), currentCC);
		}
		StoredCreditCard card = Db.getUser().getStoredCreditCards().get(0);
		Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(
			card);
		Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
		paymentButtonListener.onStoredCreditCardChosen(card);
	}

	private void paymentType(PaymentType paymentType, String cardNumber, String cardExpiration) {
		cardInfoName.setText(cardNumber);
		storedCardName.setText(cardNumber);
		if (!TextUtils.isEmpty(cardExpiration)) {
			cardInfoExpiration.setText(cardExpiration);
			cardInfoExpiration.setVisibility(VISIBLE);
			FontCache.setTypeface(cardInfoExpiration, FontCache.Font.ROBOTO_REGULAR);
		}
		else {
			FontCache.setTypeface(cardInfoName, FontCache.Font.ROBOTO_REGULAR);
			if (lineOfBusiness == LineOfBusiness.HOTELSV2) {
				cardInfoExpiration.setVisibility(VISIBLE);
				FontCache.setTypeface(cardInfoName, FontCache.Font.ROBOTO_MEDIUM);
			}
			else {
				cardInfoExpiration.setVisibility(GONE);
			}
		}
		if (paymentType != null) {
			cardInfoIcon.setImageDrawable(
				getContext().getResources().getDrawable(BookingInfoUtils.getTabletCardIcon(paymentType)));
			storedCardImageView.setImageDrawable(
				getContext().getResources().getDrawable(BookingInfoUtils.getTabletCardIcon(paymentType)));
			FontCache.setTypeface(cardInfoName, FontCache.Font.ROBOTO_MEDIUM);
			FontCache.setTypeface(cardInfoExpiration, FontCache.Font.ROBOTO_REGULAR);
		}
		else {
			cardInfoIcon.setImageDrawable(
				getContext().getResources().getDrawable(R.drawable.cars_checkout_cc_default_icon));
			storedCardImageView.setImageDrawable(
				getContext().getResources().getDrawable(R.drawable.cars_checkout_cc_default_icon));
		}
	}

	@Override
	public void setExpanded(boolean expand, boolean animate) {
		if (!isCreditCardRequired) {
			return;
		}
		super.setExpanded(expand, animate);

		if (expand) {
			if (!directlyNavigateToPaymentDetails() || ((WalletUtils.isWalletSupported(lineOfBusiness)
				&& !goToPaymentDetails()))) {
				cardInfoContainer.setVisibility(GONE);
				paymentOptionsContainer.setVisibility(VISIBLE);
				paymentOptionGoogleWallet.setVisibility(WalletUtils.isWalletSupported(lineOfBusiness) ? VISIBLE : GONE);
				billingInfoContainer.setVisibility(GONE);
				if (mToolbarListener != null) {
					mToolbarListener.showRightActionButton(false);
				}
			}
			else {
				showPaymentDetails();
			}
			if (mToolbarListener != null) {
				mToolbarListener.setActionBarTitle(getActionBarTitle());
			}
		}
		else {
			if (!WalletUtils.isWalletSupported(lineOfBusiness) && sectionBillingInfo.getBillingInfo() != null && sectionBillingInfo.getBillingInfo().isUsingGoogleWallet()) {
				reset();
			}
			cardInfoContainer.setVisibility(VISIBLE);
			paymentOptionsContainer.setVisibility(GONE);
			billingInfoContainer.setVisibility(GONE);
			bind();
			Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
			paymentButton.dismissPopup();
		}
	}

	protected boolean directlyNavigateToPaymentDetails() {
		return true;
	}

	private boolean goToPaymentDetails() {
		boolean isFilled = isFilled();
		boolean isBillingInfoValid = isFilled && sectionBillingInfo.performValidation();
		boolean isPostalCodeValid = isFilled && sectionLocation.performValidation();

		return
			// User is logged in and has a stored card
			hasStoredCard()
			// Card info user entered is valid
			|| (isBillingInfoValid && isPostalCodeValid)
			// Card info partially entered & not valid
			|| (isFilled && (!isBillingInfoValid || !isPostalCodeValid));
	}

	protected void showPaymentDetails() {
		boolean hasStoredCard = hasStoredCard();
		cardInfoContainer.setVisibility(GONE);
		paymentOptionsContainer.setVisibility(GONE);
		billingInfoContainer.setVisibility(VISIBLE);
		paymentButton.setVisibility(
			User.isLoggedIn(getContext()) && !Db.getUser().getStoredCreditCards().isEmpty() ? VISIBLE : GONE);
		storedCardContainer.setVisibility(hasStoredCard ? VISIBLE : GONE);
		sectionBillingInfo.setVisibility(hasStoredCard ? GONE : VISIBLE);
		if (hasStoredCard) {
			if (mToolbarListener != null) {
				mToolbarListener.onEditingComplete();
			}
		}
		else {
			creditCardNumber.requestFocus();
			Ui.showKeyboard(creditCardNumber, null);
		}

		// Show Debit/Credit Card hint AB test
		if (lineOfBusiness == LineOfBusiness.HOTELSV2) {
			boolean shouldShowDebitCreditHint = Db.getAbacusResponse()
					.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelCKOCreditDebitTest);
			// Omniture tracking done below -- trackHotelV2PaymentEdit()
			creditCardNumber.setHint(shouldShowDebitCreditHint ? R.string.credit_debit_card_hint : R.string.credit_card_hint);
		}

		bind();
		paymentButton.bind();
		mValidFormsOfPaymentListener.onChange();
		if (!ExpediaBookingApp.isAutomation()) {
			if (lineOfBusiness == LineOfBusiness.HOTELSV2) {
				new HotelV2Tracking().trackHotelV2PaymentEdit();

				if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelCKOPostalCodeTest)
					&& !PointOfSale.getPointOfSale().requiresHotelPostalCode()) {
					sectionLocation.setVisibility(View.GONE);
				}
			}
			else {
				OmnitureTracking.trackCheckoutPayment(lineOfBusiness);
			}
		}
		if (mToolbarListener != null) {
			mToolbarListener.setActionBarTitle(getActionBarTitle());
		}
	}

	@Override
	public boolean getMenuDoneButtonFocus() {
		if (creditCardName != null) {
			return creditCardName.hasFocus();
		}
		return false;
	}

	@Override
	public String getMenuButtonTitle() {
		return getResources().getString(R.string.Done);
	}

	@Override
	public String getActionBarTitle() {
		if (paymentOptionsContainer.getVisibility() == VISIBLE) {
			return getResources().getString(R.string.cars_payment_options_text);
		}
		else {
			return getResources().getString(R.string.cars_payment_details_text);
		}
	}

	@Override
	public void onMenuButtonPressed() {
		boolean hasStoredCard = hasStoredCard();
		boolean billingIsValid = !hasStoredCard && sectionBillingInfo.performValidation();
		boolean postalIsValid = !hasStoredCard && sectionLocation.performValidation();
		if (hasStoredCard || (billingIsValid && postalIsValid)) {
			if (shouldShowSaveDialog()) {
				showSaveBillingInfoDialog();
			}
			else {
				setExpanded(false);
			}
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

	private PaymentButton.IPaymentButtonListener paymentButtonListener = getPaymentButtonListener();

	protected PaymentButton.IPaymentButtonListener getPaymentButtonListener() {
		return new PaymentButton.IPaymentButtonListener() {
			@Override
			public void onAddNewCreditCardSelected() {

			}

			@Override
			public void onStoredCreditCardChosen(StoredCreditCard card) {
				storedCardContainer.setVisibility(VISIBLE);
				sectionBillingInfo.setVisibility(GONE);
				sectionBillingInfo.getBillingInfo().setStoredCard(card);
				setExpanded(false);
			}

			@Override
			public void onTemporarySavedCreditCardChosen(BillingInfo info) {
			}
		};
	}


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
		else if (isCreditCardRequired && (hasStoredCard())) {
			return true;
		}
		else if (isCreditCardRequired && (isFilled() && sectionBillingInfo.performValidation() && sectionLocation.performValidation())) {
			return true;
		}

		return false;
	}

	public PaymentType getCardType() {
		if (isCreditCardRequired && hasStoredCard()) {
			return sectionBillingInfo.getBillingInfo().getStoredCard().getType();
		}
		else if (isCreditCardRequired && (isFilled() && sectionBillingInfo.performValidation() && sectionLocation
			.performValidation())) {
			return sectionBillingInfo.getBillingInfo().getPaymentType();
		}

		return PaymentType.UNKNOWN;
	}

	final ISectionEditable.SectionChangeListener mValidFormsOfPaymentListener = new ISectionEditable.SectionChangeListener() {
		@Override
		public void onChange() {
			if (sectionBillingInfo == null || sectionBillingInfo.getBillingInfo() == null) {
				return;
			}
			PaymentType cardType = sectionBillingInfo.getBillingInfo().getPaymentType();
			TripBucketItem tripItem = Db.getTripBucket().getItem(lineOfBusiness);
			if (cardType != null && tripItem != null) {
				if (!tripItem.isPaymentTypeSupported(cardType)) {
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

	private void openGoogleWallet() {
		Intent i = new Intent(getContext(), GoogleWalletActivity.class);
		((AppCompatActivity)getContext()).startActivityForResult(i, REQUEST_CODE_GOOGLE_WALLET_ACTIVITY);
	}

	protected boolean hasStoredCard() {
		return sectionBillingInfo.getBillingInfo() != null && sectionBillingInfo.getBillingInfo().hasStoredCard();
	}

	protected boolean shouldShowSaveDialog() {
		return lineOfBusiness == LineOfBusiness.HOTELSV2 && User.isLoggedIn(getContext()) && !sectionBillingInfo
			.getBillingInfo().getSaveCardToExpediaAccount() && workingBillingInfoChanged()
			&& Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getStoredCard() == null;
	}

	protected void showSaveBillingInfoDialog() {
		AlertDialog dialog = new AlertDialog.Builder(getContext())
			.setTitle(R.string.save_billing_info)
			.setCancelable(false)
			.setMessage(Phrase.from(getContext(), R.string.save_billing_info_message_TEMPLATE).put("brand", BuildConfig.brand).format())
			.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					userChoosesToSaveCard();
				}
			}).setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					userChoosesNotToSaveCard();
				}
			}).create();
		dialog.show();
	}

	protected void userChoosesToSaveCard() {
		sectionBillingInfo.getBillingInfo().setSaveCardToExpediaAccount(true);
		if (directlyNavigateToPaymentDetails()) {
			setExpanded(false);
		}
		else {
			mToolbarListener.onWidgetClosed();
		}
	}

	protected void userChoosesNotToSaveCard() {
		sectionBillingInfo.getBillingInfo().setSaveCardToExpediaAccount(false);
		if (directlyNavigateToPaymentDetails()) {
			setExpanded(false);
		}
		else {
			mToolbarListener.onWidgetClosed();
		}
	}

	private boolean workingBillingInfoChanged() {
		if (sectionBillingInfo.getBillingInfo() != null) {
			return Db.getWorkingBillingInfoManager().getWorkingBillingInfo()
				.compareTo(sectionBillingInfo.getBillingInfo()) != 0;
		}
		return false;
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
}
